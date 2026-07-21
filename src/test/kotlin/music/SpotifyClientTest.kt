package music

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import dpozinen.music.spotify.SpotifyClient
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.assertj.core.api.Assertions.assertThat
import org.springframework.cloud.openfeign.support.SpringMvcContract
import kotlin.test.Test

@WireMockTest(httpPort = 9997)
class SpotifyClientTest {

	private val mapper = jacksonObjectMapper()

	private val client: SpotifyClient = Feign.builder()
		.contract(SpringMvcContract())
		.encoder(JacksonEncoder(mapper))
		.decoder(JacksonDecoder(mapper))
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

	private fun stringResource(path: String) =
		SpotifyClientTest::class.java.classLoader.getResource(path)!!.readText()
}
