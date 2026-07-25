package dpozinen.music

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties(prefix = "zoe.music")
data class MusicConfig(
	val cron: String = "0 0 3 * * *",
	val spotify: SpotifyConfig,
	val sockseek: SockseekConfig = SockseekConfig(),
	val plex: PlexConfig = PlexConfig(),
) {
	data class SpotifyConfig(
		val clientId: String,
		val clientSecret: String,
		val refreshToken: String,
		val includeCollaborative: Boolean = false,
		val additionalPlaylists: List<String> = emptyList(),
	)

	data class SockseekConfig(
		val url: String = "http://sockseek:5030",
	)

	data class PlexConfig(
		val enabled: Boolean = true,
		val libraryId: Int = 11,
		val sockseekPathPrefix: String = "/music/",
		val plexPathPrefix: String = "/media/chute/Song/",
		val wait: Wait = Wait(),
	) {
		data class Wait(
			val interval: Duration = Duration.ofSeconds(30),
			val maxAttempts: Int = 240,
		)
	}
}
