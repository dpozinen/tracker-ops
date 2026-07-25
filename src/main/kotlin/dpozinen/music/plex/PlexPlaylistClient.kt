package dpozinen.music.plex

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import feign.RequestInterceptor
import feign.RequestTemplate
import feign.Retryer
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.*

@FeignClient(
	name = "plex-playlist",
	url = "\${zoe.plex.url}",
	configuration = [PlexPlaylistClient.Config::class],
)
interface PlexPlaylistClient {

	@GetMapping("/identity", headers = ["Accept=application/json"])
	fun identity(): PlexIdentity

	@GetMapping("/library/sections", headers = ["Accept=application/json"])
	fun sections(): PlexSections

	@GetMapping("/library/sections/{id}/refresh")
	fun scan(@PathVariable id: Int)

	@GetMapping("/library/sections/{id}/all", headers = ["Accept=application/json"])
	fun tracks(
		@PathVariable id: Int,
		@RequestHeader("X-Plex-Container-Start") start: Int,
		@RequestHeader("X-Plex-Container-Size") size: Int,
		@RequestParam type: Int = 10,
	): PlexTracks

	@GetMapping("/playlists", headers = ["Accept=application/json"])
	fun playlists(): PlexPlaylists

	@GetMapping("/playlists/{id}/items", headers = ["Accept=application/json"])
	fun playlistItems(@PathVariable id: String): PlexPlaylistItems

	@PostMapping("/playlists", headers = ["Accept=application/json"])
	fun createPlaylist(
		@RequestParam title: String,
		@RequestParam uri: String,
		@RequestParam type: String = "audio",
		@RequestParam smart: Int = 0,
	): PlexPlaylists

	@PutMapping("/playlists/{id}/items")
	fun addItems(@PathVariable id: String, @RequestParam uri: String)

	@DeleteMapping("/playlists/{id}/items/{itemId}")
	fun removeItem(@PathVariable id: String, @PathVariable itemId: Int)

	open class Config {
		@Bean open fun retryer() = Retryer.Default()
		@Bean open fun plexAuth(@Value("\${zoe.plex.api-key}") key: String) =
			RequestInterceptor { t: RequestTemplate -> t.query("X-Plex-Token"); t.query("X-Plex-Token", key) }
	}
}

data class PlexIdentity(@param:JsonProperty("MediaContainer") val container: Id) {
	data class Id(val machineIdentifier: String)
}

data class PlexSections(@param:JsonProperty("MediaContainer") val container: Dir) {
	data class Dir(@param:JsonProperty("Directory") val directory: List<PlexDirectory> = emptyList())
}
data class PlexDirectory(val key: String, val refreshing: Boolean = false)

data class PlexTracks(@param:JsonProperty("MediaContainer") val container: Container) {
	// ponytail: ignoreUnknown covers "offset" and other Plex-version fields we don't need
	@JsonIgnoreProperties(ignoreUnknown = true)
	data class Container(
		val totalSize: Int = 0,
		val size: Int = 0,
		@param:JsonProperty("Metadata") val metadata: List<PlexTrack> = emptyList(),
	)
}
data class PlexTrack(
	val ratingKey: String,
	@param:JsonProperty("Media") val media: List<PlexMedia> = emptyList(),
) {
	fun paths(): List<String> = media.flatMap { it.part }.map { it.file }
}
data class PlexMedia(@param:JsonProperty("Part") val part: List<PlexPart> = emptyList())
data class PlexPart(val file: String)

data class PlexPlaylists(@param:JsonProperty("MediaContainer") val container: Container) {
	data class Container(@param:JsonProperty("Metadata") val metadata: List<PlexPlaylist> = emptyList())
}
data class PlexPlaylist(val ratingKey: String, val title: String, val playlistType: String? = null)

data class PlexPlaylistItems(@param:JsonProperty("MediaContainer") val container: Container) {
	data class Container(@param:JsonProperty("Metadata") val metadata: List<PlexPlaylistItem> = emptyList())
}
data class PlexPlaylistItem(val ratingKey: String, val playlistItemID: Int)
