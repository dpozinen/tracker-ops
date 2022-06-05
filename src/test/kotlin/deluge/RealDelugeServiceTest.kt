package deluge

import Data
import Data.Companion.httpHeaders
import dpozinen.deluge.core.DelugeState
import dpozinen.deluge.core.RealDelugeService
import dpozinen.deluge.mutations.Search
import dpozinen.deluge.rest.DelugeClient
import dpozinen.deluge.rest.DelugeParams
import dpozinen.deluge.rest.DelugeResponse
import dpozinen.deluge.rest.DelugeConverter
import io.mockk.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.ResponseEntity
import kotlin.test.Test


class RealDelugeServiceTest {

    @Test
    fun `should parse response to torrents`() {
        val (client, converter) = mock()
        val service = RealDelugeService("", client, converter)

        assertThat(service.statefulTorrents())
            .hasSize(1)
            .first()
            .isEqualTo(Data.delugeTorrent)
    }

    @Test
    fun `should throw if result has no torrents`() {
        val (client, converter) = mock(mockTorrents = false)
        val service = RealDelugeService("", client, converter)

        try {
            service.statefulTorrents()
        } catch (e: Exception) {
            assertThat(e)
                .hasMessage("no torrents")
                .isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Test
    fun `should login once per active session`() {
        val (client, converter) = mock()
        val service = RealDelugeService("", client, converter)

        service.statefulTorrents()
        service.statefulTorrents()
        service.statefulTorrents()

        verify(exactly = 1) { client.login() }
    }

    @Test
    fun `should perform mutations concurrently`() {
        val (client, converter) = mock()
        val service = RealDelugeService("", client, converter)

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
        val (client, converter) = mock(mockTorrents = true, disconnected = true)
        val service = RealDelugeService("", client, converter)

        service.statefulTorrents()

        verify(exactly = 1) { client.connect(Data.sessionIdHttpCookie) }
        verify(exactly = 2) { client.login() }
        verify(exactly = 2) { client.torrents(DelugeParams.torrents(), Data.sessionIdHttpCookie) }
    }

    @Test
    fun `should collect stats`() {
        val (client, converter) = mock(mockTorrents = true, disconnected = true, mockConverter = true)
        val service = RealDelugeService("", client, converter)

        every { converter.convert((any() as Map.Entry<String, Map<String, *>>)) } returns Data.delugeTorrent

        val torrents = (1..100).map { converter.convert(Data.delugeTorrentResponse.entries.first()) }

        val stats = service.statsFrom(torrents, torrents)

        assertThat(stats).isEqualTo(Data.stats)
    }

    private fun mock(mockTorrents: Boolean = true, disconnected: Boolean = false, mockConverter: Boolean = false)
        : Pair<DelugeClient, DelugeConverter> {
        val delugeClient = mockk<DelugeClient>()
        val response = mockk<ResponseEntity<DelugeResponse>>()
        val converter = if (mockConverter) mockk() else DelugeConverter()

        every { response.body } returns DelugeResponse(mapOf<String, Any>(), 123, mapOf())
        every { response.headers } returns httpHeaders()
        every { response.body.disconnected() } returns disconnected
        every { delugeClient.connect(Data.sessionIdHttpCookie) } just runs

        if (mockTorrents)
            every { response.body.torrents() } returns Data.delugeTorrentResponse
        else
            every { response.body.torrents() } returns emptyMap()

        every { delugeClient.login() } returns response
        every { delugeClient.torrents(DelugeParams.torrents(), Data.sessionIdHttpCookie) } returns response

        return delugeClient to converter
    }

}

private fun RealDelugeService.reflectExtractState(): DelugeState {
    return RealDelugeService::class.java.getDeclaredField("state").let {
        it.isAccessible = true
        return@let it.get(this)
    } as DelugeState
}