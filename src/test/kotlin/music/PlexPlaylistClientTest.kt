package music

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import dpozinen.music.plex.PlexPlaylistClient
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.assertj.core.api.Assertions.assertThat
import org.springframework.cloud.openfeign.support.SpringMvcContract
import kotlin.test.Test

@WireMockTest(httpPort = 9995)
class PlexPlaylistClientTest {

	private val mapper = jacksonObjectMapper()
	private val client: PlexPlaylistClient = Feign.builder()
		.contract(SpringMvcContract())
		.encoder(JacksonEncoder(mapper))
		.decoder(JacksonDecoder(mapper))
		.target(PlexPlaylistClient::class.java, "http://localhost:9995")

	private fun res(path: String) =
		PlexPlaylistClientTest::class.java.classLoader.getResource(path)!!.readText()

	@Test
	fun `identity returns machine id`() {
		stubFor(get(urlPathEqualTo("/identity")).willReturn(okJson(res("plex/identity.json"))))
		assertThat(client.identity().container.machineIdentifier).isEqualTo("machine-abc")
	}

	@Test
	fun `tracks exposes rating key and file paths`() {
		stubFor(get(urlPathEqualTo("/library/sections/11/all"))
			.withQueryParam("type", equalTo("10"))
			.willReturn(okJson(res("plex/tracks-page1.json"))))

		val page = client.tracks(11, start = 0, size = 100)

		assertThat(page.container.metadata.map { it.ratingKey to it.paths() }).isEqualTo(listOf(
			"101" to listOf("/media/chute/Song/6YNTHMANE/FINA LANA (SLOWED)/01. FINA LANA (SLOWED).flac"),
			"102" to listOf("/media/chute/Song/2Pac/Live/04. Hit Em Up.flac"),
		))
		assertThat(page.container.totalSize).isEqualTo(2)
	}

	@Test
	fun `playlist items expose rating key and integer item id`() {
		stubFor(get(urlPathEqualTo("/playlists/900/items")).willReturn(okJson(res("plex/playlist-items.json"))))

		assertThat(client.playlistItems("900").container.metadata.map { it.ratingKey to it.playlistItemID })
			.isEqualTo(listOf("101" to 5001, "199" to 5002))
	}

	@Test
	fun `empty playlist without Metadata deserializes to empty list`() {
		stubFor(get(urlPathEqualTo("/playlists/901/items"))
			.willReturn(okJson("""{"MediaContainer":{"size":0}}""")))

		assertThat(client.playlistItems("901").container.metadata).isEmpty()
	}
}
