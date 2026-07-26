package deluge

import dpozinen.deluge.rest.clients.DelugeResponseDecoder
import dpozinen.deluge.rest.clients.DelugeResult
import dpozinen.errors.DelugeDisconnectedException
import dpozinen.errors.DelugeSessionExpiredException
import feign.Request
import feign.Response
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import tools.jackson.core.type.TypeReference
import java.lang.reflect.Type
import kotlin.text.Charsets.UTF_8
import kotlin.test.Test

class DelugeResponseDecoderTest {

	private val decoder = DelugeResponseDecoder()
	private val booleanResult: Type = object : TypeReference<DelugeResult<Boolean>>() {}.type

	private fun response(body: String): Response =
		Response.builder()
			.status(200)
			.reason("OK")
			.request(Request.create(Request.HttpMethod.POST, "http://deluge/json", emptyMap(), null, null, null))
			.headers(mapOf("Content-Type" to listOf("application/json")))
			.body(body, UTF_8)
			.build()

	@Test
	fun `rewrites null result to true and decodes via jackson 3`() {
		val decoded = decoder.decode(response("""{"result": null, "error": null, "id": 1}"""), booleanResult)

		assertThat(decoded).isEqualTo(DelugeResult(result = true, error = null, id = 1))
	}

	@Test
	fun `decodes a real boolean result unchanged`() {
		val decoded = decoder.decode(response("""{"result": false, "error": null, "id": 7}"""), booleanResult)

		assertThat(decoded).isEqualTo(DelugeResult(result = false, error = null, id = 7))
	}

	@Test
	fun `throws session expired when not authenticated`() {
		assertThatThrownBy { decoder.decode(response("""{"error": "Not authenticated"}"""), booleanResult) }
			.isInstanceOf(DelugeSessionExpiredException::class.java)
	}

	@Test
	fun `throws disconnected when deluge reports not connected`() {
		assertThatThrownBy { decoder.decode(response("""{"result": {"connected": false}}"""), booleanResult) }
			.isInstanceOf(DelugeDisconnectedException::class.java)
	}
}
