package deluge

import Data
import com.github.tomakehurst.wiremock.client.ScenarioMappingBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.github.tomakehurst.wiremock.stubbing.Scenario.STARTED
import com.marcinziolo.kotlin.wiremock.*
import deluge.Bodies.Companion.stringResource
import dpozinen.App
import dpozinen.deluge.rest.DelugeRequest
import dpozinen.deluge.rest.clients.DelugeActionsClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest(
    classes = [App::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
@WireMockTest(httpPort = 8112)
class DelugeFeignClientFlowTest {

    @Autowired
    lateinit var client: DelugeActionsClient

    /**
     * Upon [DelugeActionsClient] calls [AuthInterceptor][dpozinen.deluge.rest.clients.DelugeAuthInterceptor] injects a session cookie.
     * If the cookie is expired [DelugeAuthClient][dpozinen.deluge.rest.clients.DelugeAuthClient] fetches it from deluge.
     *
     * First response to get torrents is mocked to return a disconnected response.
     * This makes [DelugeResponseDecoder][dpozinen.deluge.rest.clients.DelugeResponseDecoder] throw an exception which is then
     * handled in [AuthConnectRetryer][dpozinen.deluge.rest.clients.AuthConnectRetryer]
     * using the [DelugeConnectionClient][dpozinen.deluge.rest.clients.DelugeConnectionClient].
     *
     * Second response to get torrents is mocked to return a good response.
     */
    @Test
    fun `should login and handle disconnect when getting torrents`() {
        mockDeluge(Bodies.loginRequest, Bodies.loginResponse) and {
            header = "Set-Cookie" to Data.sessionIdCookie
        }
        mockDeluge(Bodies.hostsRequest, Bodies.hostsResponse)
        mockDeluge(Bodies.connectRequest, Bodies.connectResponse)
        mockDeluge(Bodies.moveRequest, Bodies.moveResponse)

        mockInConnectScenario {
            whenScenarioStateIs(STARTED)
                .willReturn(okJson(Bodies.disconnectedResponse))
                .willSetStateTo("Connected")
        }

        mockInConnectScenario {
            whenScenarioStateIs("Connected")
                .willReturn(okJson(stringResource("/deluge-torrents-response.json")))
        }

        val torrents = client.torrents(DelugeRequest.torrents()).result.torrents()
        assertTorrents(torrents)
    }

    private fun mockDeluge(request: String, response: String) = mockPost {
        url equalTo "/json"
        body equalTo request
    } returnsJson {
        body = response
    }

    private fun mockInConnectScenario(builder: ScenarioMappingBuilder.() -> ScenarioMappingBuilder) {
        stubFor(
            builder(
                post(urlEqualTo("/json"))
                    .withRequestBody(equalToJson(stringResource("/deluge-torrents-request.json")))
                    .inScenario("Connect Scenario")
                    .whenScenarioStateIs("Connected")
                    .willReturn(okJson(stringResource("/deluge-torrents-response.json")))
            )
        )
    }
}