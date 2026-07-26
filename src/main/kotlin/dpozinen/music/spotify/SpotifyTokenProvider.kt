package dpozinen.music.spotify

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import dpozinen.SpringFeignCodec
import dpozinen.music.MusicConfig
import feign.Feign
import feign.RequestInterceptor
import feign.RequestTemplate
import mu.KotlinLogging.logger
import org.springframework.cloud.openfeign.support.SpringMvcContract
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import java.time.Instant
import java.util.*

class SpotifyTokenProvider(
	private val config: MusicConfig.SpotifyConfig,
	tokenBaseUrl: String = "https://accounts.spotify.com",
) : RequestInterceptor {

	private val log = logger {}
	private var token: String = ""
	private var expiresAt: Instant = Instant.EPOCH

	// 429s on the token endpoint are handled by SpotifyRetryer, same as the API client
	private val auth: SpotifyAuthClient = Feign.builder()
		.contract(SpringMvcContract())
		.encoder(SpringFeignCodec.encoder())
		.decoder(SpringFeignCodec.decoder())
		.retryer(SpotifyRetryer())
		.target(SpotifyAuthClient::class.java, tokenBaseUrl)

	override fun apply(template: RequestTemplate) {
		if (Instant.now().isAfter(expiresAt.minusSeconds(30))) refresh()
		template.header("Authorization", "Bearer $token")
	}

	// ponytail: no locking — concurrent first-calls race harmlessly, both tokens are valid
	private fun refresh() {
		val credentials = Base64.getEncoder()
			.encodeToString("${config.clientId}:${config.clientSecret}".toByteArray())

		val response = auth.token(
			authorization = "Basic $credentials",
			body = "grant_type=refresh_token&refresh_token=${config.refreshToken}",
		)

		token = response.accessToken
		expiresAt = Instant.now().plusSeconds(response.expiresIn)
		log.info { "Spotify token refreshed, expires at $expiresAt" }
	}
}

interface SpotifyAuthClient {
	@PostMapping(value = ["/api/token"], consumes = [MediaType.APPLICATION_FORM_URLENCODED_VALUE])
	fun token(
		@RequestHeader("Authorization") authorization: String,
		@RequestBody body: String,
	): TokenResponse
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class TokenResponse(
	@param:JsonProperty("access_token") val accessToken: String,
	@param:JsonProperty("expires_in") val expiresIn: Long,
)
