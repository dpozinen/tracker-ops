package dpozinen.music.spotify

import com.fasterxml.jackson.annotation.JsonProperty
import dpozinen.music.MusicConfig
import feign.RequestInterceptor
import feign.RequestTemplate
import mu.KotlinLogging.logger
import org.springframework.web.client.RestClient
import org.springframework.web.client.body
import java.time.Instant
import java.util.*

class SpotifyTokenProvider(
	private val config: MusicConfig.SpotifyConfig,
	private val tokenBaseUrl: String = "https://accounts.spotify.com",
) : RequestInterceptor {

	private val log = logger {}
	private var token: String = ""
	private var expiresAt: Instant = Instant.EPOCH

	private val client = RestClient.builder().baseUrl(tokenBaseUrl).build()

	override fun apply(template: RequestTemplate) {
		if (Instant.now().isAfter(expiresAt.minusSeconds(30))) refresh()
		template.header("Authorization", "Bearer $token")
	}

	// ponytail: no locking — concurrent first-calls race harmlessly, both tokens are valid
	private fun refresh() {
		val credentials = Base64.getEncoder()
			.encodeToString("${config.clientId}:${config.clientSecret}".toByteArray())

		val response = client.post()
			.uri("/api/token")
			.header("Authorization", "Basic $credentials")
			.header("Content-Type", "application/x-www-form-urlencoded")
			.body("grant_type=refresh_token&refresh_token=${config.refreshToken}")
			.retrieve()
			.body<TokenResponse>()!!

		token = response.accessToken
		expiresAt = Instant.now().plusSeconds(response.expiresIn)
		log.info { "Spotify token refreshed, expires at $expiresAt" }
	}

	private data class TokenResponse(
		@param:JsonProperty("access_token") val accessToken: String,
		@param:JsonProperty("expires_in") val expiresIn: Long,
	)
}
