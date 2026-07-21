package music

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import dpozinen.music.sockseek.SockseekClient
import dpozinen.music.sockseek.SockseekJobRequest
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.assertj.core.api.Assertions.assertThat
import org.springframework.cloud.openfeign.support.SpringMvcContract
import kotlin.test.Test

@WireMockTest(httpPort = 9996)
class SockseekClientTest {

	private val mapper = jacksonObjectMapper()

	private val client: SockseekClient = Feign.builder()
		.contract(SpringMvcContract())
		.encoder(JacksonEncoder(mapper))
		.decoder(JacksonDecoder(mapper))
		.target(SockseekClient::class.java, "http://localhost:9996")

	@Test
	fun `submitJob posts source url and returns job response`() {
		stubFor(
			post(urlEqualTo("/api/download"))
				.withRequestBody(matchingJsonPath("$.url", equalTo("https://open.spotify.com/playlist/abc")))
				.willReturn(okJson("""{"id":"abc123","status":"queued"}"""))
		)

		val response = client.submitJob(SockseekJobRequest("https://open.spotify.com/playlist/abc"))

		assertThat(response.id).isEqualTo("abc123")
	}
}
