package dpozinen.music.spotify

import dpozinen.music.MusicConfig
import feign.Retryer
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
	name = "spotify",
	url = "https://api.spotify.com",
	configuration = [SpotifyClient.Config::class],
)
interface SpotifyClient {

	@GetMapping("/v1/me")
	fun getMe(): SpotifyUser

	@GetMapping("/v1/me/playlists")
	fun getPlaylists(
		@RequestParam limit: Int = 50,
		@RequestParam offset: Int = 0,
	): SpotifyPagedPlaylists

	open class Config {
		@Bean
		open fun retryer(): Retryer = SpotifyRetryer()

		@Bean
		open fun spotifyAuth(config: MusicConfig) = SpotifyTokenProvider(config.spotify)
	}
}

data class SpotifyUser(val id: String)

data class SpotifyPagedPlaylists(
	val items: List<SpotifyPlaylist>,
	val next: String?,
)

data class SpotifyPlaylist(
	val id: String,
	val name: String,
	val owner: SpotifyOwner,
	val collaborative: Boolean,
)

data class SpotifyOwner(val id: String)
