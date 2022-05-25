package deluge

import Data
import Data.Companion.httpHeaders
import dpozinen.deluge.*
import dpozinen.deluge.mutations.Search
import io.mockk.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.ResponseEntity
import kotlin.test.Test


class RealDelugeServiceTest {

    @Test
    fun `should parse response to torrents`() {
        val service = RealDelugeService("", mock())

        assertThat(service.torrents())
            .hasSize(1)
            .first()
            .isEqualTo(Data.delugeTorrent)
    }

    @Test
    fun `should throw if result has no torrents`() {
        val delugeClient = mock(mockTorrents = false)

        val service = RealDelugeService("", delugeClient)

        try {
            service.torrents()
        } catch (e: Exception) {
            assertThat(e)
                .hasMessage("no torrents")
                .isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Test
    fun `should login once per active session`() {
        val delugeClient = mock()

        val service = RealDelugeService("", delugeClient)

        service.torrents()
        service.torrents()
        service.torrents()

        verify(exactly = 1) { delugeClient.login() }
    }

    @Test
    fun `should perform mutations concurrently`() {
        val service = RealDelugeService("", mock())

        fun search(range: IntRange) =
            range.map { Search(it.toString()) }.forEach { service.mutate(it) }

        runBlocking {
            launch { search(1..100) }
            launch { search(101..200) }
            launch { search(201..300) }
        }

        assertThat(service.reflectExtractState().mutations).hasSize(300)
    }

    @Test
    fun `should re connect to deluge`() {
        val client = mock(mockTorrents = true, connected = false)
        val service = RealDelugeService("", client)

        service.torrents()

        verify(exactly = 1) { client.connect(Data.sessionIdHttpCookie) }
        verify(exactly = 2) { client.login() }
        verify(exactly = 2) { client.torrents(DelugeParams.torrents(), Data.sessionIdHttpCookie) }

    }

    private fun mock(mockTorrents: Boolean = true, connected: Boolean = true): DelugeClient {
        val delugeClient = mockk<DelugeClient>()
        val response = mockk<ResponseEntity<DelugeResponse>>()

        every { response.body } returns DelugeResponse(mapOf<String, Any>(), 123, mapOf())
        every { response.headers } returns httpHeaders()
        every { response.body.isConnected() } returns connected
        every { delugeClient.connect(Data.sessionIdHttpCookie) } just runs

        if (mockTorrents)
            every { response.body.torrents() } returns Data.delugeTorrentResponse

        every { delugeClient.login() } returns response
        every { delugeClient.torrents(DelugeParams.torrents(), Data.sessionIdHttpCookie) } returns response

        return delugeClient
    }

}

private fun RealDelugeService.reflectExtractState(): DelugeState {
    return RealDelugeService::class.java.getDeclaredField("state").let {
        it.isAccessible = true
        return@let it.get(this)
    } as DelugeState
}