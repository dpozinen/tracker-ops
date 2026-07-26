package music

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import dpozinen.SpringFeignCodec
import dpozinen.music.plex.PlexPlaylistClient
import dpozinen.music.plex.PlexPlaylistItems
import feign.Feign
import org.assertj.core.api.Assertions.assertThat
import org.springframework.cloud.openfeign.support.SpringMvcContract
import tools.jackson.databind.json.JsonMapper
import kotlin.test.Test

@WireMockTest(httpPort = 9995)
class PlexPlaylistClientTest {

	private val client: PlexPlaylistClient = Feign.builder()
		.contract(SpringMvcContract())
		.encoder(SpringFeignCodec.encoder())
		.decoder(SpringFeignCodec.decoder())
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

	// Production decodes with Spring Boot 4's Jackson 3 mapper (tools.jackson) + auto-discovered
	// kotlin module. Verify our DTOs apply their emptyList() defaults there — an empty Plex
	// playlist omits "Metadata" entirely, which previously NPE'd the non-null param.
	@Test
	fun `plex dtos apply defaults under jackson 3 kotlin module`() {
		val jackson3 = JsonMapper.builder().findAndAddModules().build()

		val items = jackson3.readValue("""{"MediaContainer":{"size":0}}""", PlexPlaylistItems::class.java)

		assertThat(items.container.metadata).isEmpty()
	}
}
