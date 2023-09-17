package deluge

import Data
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import com.marcinziolo.kotlin.wiremock.*
import com.ninjasquad.springmockk.MockkBean
import deluge.Bodies.Companion.stringResource
import dpozinen.App
import dpozinen.deluge.rest.DelugeRequest
import dpozinen.deluge.rest.DelugeSessionHolder
import dpozinen.deluge.rest.clients.*
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.test.context.ActiveProfiles
import kotlin.test.BeforeTest
import kotlin.test.Test

@SpringBootTest(
    classes = [App::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
@WireMockTest(httpPort = 8112)
class DelugeFeignClientsTest {

    @Autowired
    lateinit var client: DelugeActionsClient

    @Autowired
    lateinit var authClient: DelugeAuthClient

    @Autowired
    lateinit var connectionClient: DelugeConnectionClient

    @Autowired
    lateinit var plexClient: PlexClient

    @Autowired
    lateinit var trueNasClient: TrueNasClient

    @MockkBean
    lateinit var delugeSessionHolder: DelugeSessionHolder

    @BeforeTest
    fun cleanup() {
        every { delugeSessionHolder.get() } returns "dummy"
        reset()
    }

    @Test
    fun `should login`() {
        mockDeluge(Bodies.loginRequest, Bodies.loginResponse) and {
            header = "Set-Cookie" to Data.sessionIdCookie
        }

        val response = authClient.login()

        assertThat(response.headers())
            .extracting { it["Set-Cookie"]?.first() }
            .isEqualTo(Data.sessionIdCookie)
    }

    @Test
    fun `should connect`() {
        mockDeluge(Bodies.hostsRequest, Bodies.hostsResponse)
        mockDeluge(Bodies.connectRequest, Bodies.connectResponse)

        val id = connectionClient.hosts(DelugeRequest.hosts()).result.id()
        assertThat(id).isEqualTo("8c0f8366771f4859a48d7cc61dbd9e1f")

        connectionClient.connect(DelugeRequest.connect(id))
    }

    @Test
    fun `should get torrents`() {
        mockDeluge(
            stringResource("/deluge-torrents-request.json"),
            stringResource("/deluge-torrents-response.json")
        )

        val torrents = client.torrents().result.torrents()
        assertTorrents(torrents)
    }

    @Test
    fun `should add magnet`() {
        mockDeluge(Bodies.addMagnetRequest, Bodies.addMagnetResponse)

        val result = client.addMagnet(DelugeRequest.addMagnet("magnet", "folder")).result
        assertThat(result).isTrue()
    }

    @Test
    fun `should move torrent download folder`() {
        mockDeluge(Bodies.moveRequest, Bodies.moveResponse)

        val result = client.move(
            DelugeRequest.move(
                listOf(
                    "ee21ac410a4df9d2a09a97a6890fc74c0d143a0b",
                    "776551013d0d91c1d58674be34ebff91ec0c4b94"
                ), "/Downloads/Show"
            )
        ).result

        assertThat(result).isTrue()
    }

    @Test
    fun `should trigger cron job`() {
        mockPost {
            url equalTo "/api/v2.0/cronjob/run"
            body equalTo """{"id":1, "skip_disabled":false}"""
            headers contains AUTHORIZATION to "Bearer truenas-key"
        } returns {
            statusCode = 200
        }
        trueNasClient.startCronJob()
    }

    @Test
    fun `should trigger lib scan`() {
        mockGet {
            url equalTo "/library/sections/1/refresh"
            queryParams contains "X-Plex-Token" to "plex-key"
        } returns {
            statusCode = 200
        }
        plexClient.scanLibrary(1)
    }

    private fun mockDeluge(request: String, response: String) = mockPost {
        url equalTo "/json"
        body equalTo request
    } returnsJson {
        body = response
    }

}