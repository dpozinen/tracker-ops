package dpozinen.music

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "zoe.music")
data class MusicConfig(
	val cron: String = "0 0 3 * * *",
	val spotify: SpotifyConfig,
	val sockseek: SockseekConfig = SockseekConfig(),
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
}
