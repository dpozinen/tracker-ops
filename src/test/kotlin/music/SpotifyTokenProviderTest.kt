package music

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import dpozinen.music.MusicConfig
import dpozinen.music.spotify.SpotifyTokenProvider
import feign.RequestTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

@WireMockTest(httpPort = 9998)
class SpotifyTokenProviderTest {

	private lateinit var provider: SpotifyTokenProvider

	@BeforeEach
	fun setup() {
		val config = MusicConfig.SpotifyConfig(
			clientId = "test-id",
			clientSecret = "test-secret",
			refreshToken = "test-refresh",
		)
		provider = SpotifyTokenProvider(config, "http://localhost:9998")
	}

	@Test
	fun `injects bearer token from spotify token endpoint`() {
		stubFor(
			post(urlEqualTo("/api/token"))
				.willReturn(okJson("""{"access_token":"tok123","token_type":"Bearer","expires_in":3600,"scope":"playlist-read-private"}"""))
		)

		val template = RequestTemplate()
		provider.apply(template)

		assertThat(template.headers()["Authorization"]).isEqualTo(listOf("Bearer tok123"))
	}

	@Test
	fun `reuses cached token without re-fetching`() {
		stubFor(
			post(urlEqualTo("/api/token"))
				.willReturn(okJson("""{"access_token":"tok123","token_type":"Bearer","expires_in":3600,"scope":"playlist-read-private"}"""))
		)

		val template = RequestTemplate()
		provider.apply(template)
		provider.apply(template)

		verify(1, postRequestedFor(urlEqualTo("/api/token")))
	}

	@Test
	fun `retries token fetch on 429`() {
		stubFor(post(urlEqualTo("/api/token")).inScenario("rate-limit")
			.whenScenarioStateIs(STARTED)
			.willReturn(aResponse().withStatus(429).withHeader("Retry-After", "0"))
			.willSetStateTo("recovered"))
		stubFor(post(urlEqualTo("/api/token")).inScenario("rate-limit")
			.whenScenarioStateIs("recovered")
			.willReturn(okJson("""{"access_token":"tok123","token_type":"Bearer","expires_in":3600,"scope":"playlist-read-private"}""")))

		val template = RequestTemplate()
		provider.apply(template)

		assertThat(template.headers()["Authorization"]).isEqualTo(listOf("Bearer tok123"))
		verify(2, postRequestedFor(urlEqualTo("/api/token")))
	}
}
