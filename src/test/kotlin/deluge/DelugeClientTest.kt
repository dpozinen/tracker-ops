package deluge

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.marcinziolo.kotlin.wiremock.*
import dpozinen.App
import dpozinen.deluge.rest.DelugeClient
import dpozinen.deluge.rest.DelugeResponse
import dpozinen.errors.DelugeClientException
import dpozinen.errors.DelugeServerDownException
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest(
    classes = [App::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@WireMockTest(httpPort = 8112)
@ActiveProfiles("test")
class DelugeClientTest {

    @Autowired
    lateinit var client: DelugeClient

    @Test
    fun `should login`() {
        mockPost {
            url equalTo "/json"
            body equalTo Bodies.loginRequest
        } returnsJson {
            body = Bodies.loginResponse
        } and {
            header = "Set-Cookie" to Data.sessionIdCookie
        }

        val response = client.login()

        assertThat(response.body)
            .isEqualTo(
                DelugeResponse(result = true, id = 8888, error = null)
            )
    }

    @Test
    fun `should throw when server down`() {
        stubFor(post(urlEqualTo("/json"))
            .willReturn(aResponse().withFault(Fault.CONNECTION_RESET_BY_PEER)))

        assertThrows<DelugeServerDownException> { client.login() }

        reset()
    }

    @Test
    fun `should throw when null result`() {
        mockPost {
            url equalTo "/json"
            body equalTo Bodies.loginRequest
        } returnsJson {
            body = """ { "result": null, "error": {}, "id": 109384 } """
        }

        assertThrows<DelugeClientException> { client.login() }
    }

}