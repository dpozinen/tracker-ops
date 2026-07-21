package music

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
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
				.willReturn(okJson("""{"access_token":"tok123","expires_in":3600}"""))
		)

		val template = RequestTemplate()
		provider.apply(template)

		assertThat(template.headers()["Authorization"]).isEqualTo(listOf("Bearer tok123"))
	}

	@Test
	fun `reuses cached token without re-fetching`() {
		stubFor(
			post(urlEqualTo("/api/token"))
				.willReturn(okJson("""{"access_token":"tok123","expires_in":3600}"""))
		)

		val template = RequestTemplate()
		provider.apply(template)
		provider.apply(template)

		verify(1, postRequestedFor(urlEqualTo("/api/token")))
	}
}
