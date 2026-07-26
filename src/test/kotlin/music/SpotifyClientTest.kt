package music

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import dpozinen.SpringFeignCodec
import dpozinen.music.spotify.SpotifyClient
import dpozinen.music.spotify.SpotifyRetryer
import feign.Feign
import org.assertj.core.api.Assertions.assertThat
import org.springframework.cloud.openfeign.support.SpringMvcContract
import kotlin.test.Test

@WireMockTest(httpPort = 9997)
class SpotifyClientTest {

	private val client: SpotifyClient = Feign.builder()
		.contract(SpringMvcContract())
		.encoder(SpringFeignCodec.encoder())
		.decoder(SpringFeignCodec.decoder())
		.retryer(SpotifyRetryer(defaultWaitMs = 0))
		.target(SpotifyClient::class.java, "http://localhost:9997")

	@Test
	fun `getMe returns user id`() {
		stubFor(get(urlEqualTo("/v1/me")).willReturn(okJson(stringResource("spotify/me.json"))))

		val user = client.getMe()

		assertThat(user.id).isEqualTo("dpozinen")
	}

	@Test
	fun `getPlaylists returns paged response`() {
		stubFor(
			get(urlPathEqualTo("/v1/me/playlists"))
				.withQueryParam("limit", equalTo("50"))
				.withQueryParam("offset", equalTo("0"))
				.willReturn(okJson(stringResource("spotify/playlists-page1.json")))
		)

		val page = client.getPlaylists(50, 0)

		assertThat(page.items).hasSize(2)
		assertThat(page.items[0].owner.id).isEqualTo("dpozinen")
		assertThat(page.next).isNull()
	}

	@Test
	fun `retries on 429 then succeeds`() {
		stubFor(get(urlEqualTo("/v1/me")).inScenario("rate-limit")
			.whenScenarioStateIs(STARTED)
			.willReturn(aResponse().withStatus(429).withHeader("Retry-After", "0"))
			.willSetStateTo("recovered"))
		stubFor(get(urlEqualTo("/v1/me")).inScenario("rate-limit")
			.whenScenarioStateIs("recovered")
			.willReturn(okJson(stringResource("spotify/me.json"))))

		val user = client.getMe()

		assertThat(user.id).isEqualTo("dpozinen")
		verify(2, getRequestedFor(urlEqualTo("/v1/me")))
	}

	private fun stringResource(path: String) =
		SpotifyClientTest::class.java.classLoader.getResource(path)!!.readText()
}
