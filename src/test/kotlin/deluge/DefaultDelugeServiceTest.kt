package deluge

import Data
import Data.Companion.delugeTorrentResponse
import dpozinen.deluge.core.DefaultDelugeService
import dpozinen.deluge.core.DelugeDownloadFollower
import dpozinen.deluge.core.DelugeState
import dpozinen.deluge.mutations.Search
import dpozinen.deluge.rest.DelugeConverter
import dpozinen.deluge.rest.clients.DelugeActionsClient
import dpozinen.deluge.rest.clients.DelugeResult
import dpozinen.deluge.rest.clients.TorrentsResult
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test


class DefaultDelugeServiceTest {

    private val follower: DelugeDownloadFollower = mockk()

    @Test
    fun `should parse response to torrents`() {
        val (client, converter) = mock()
        val service = DefaultDelugeService("", client, converter, follower)

        assertThat(service.statefulTorrents())
            .hasSize(1)
            .first()
            .isEqualTo(Data.delugeTorrent)
    }

    @Test
    fun `should perform mutations concurrently`() {
        val (client, converter) = mock()
        val service = DefaultDelugeService("", client, converter, follower)

        fun search(range: IntRange) =
            range.map { Search(it.toString()) }.forEach { service.mutate(it) }

        runBlocking {
            launch { search(1..100) }
            launch { search(101..200) }
            launch { search(201..300) }
        }

        assertThat(service.reflectExtractState().mutations).hasSize(301)
    }

    @Test
    fun `should collect stats`() {
        val (client, converter) = mock(mockConverter = true)
        val service = DefaultDelugeService("", client, converter, follower)

        val torrents = (1..100).map { delugeTorrentResponse }

        val stats = service.info(torrents, torrents)

        assertThat(stats).isEqualTo(Data.info)
    }

    private fun mock(mockConverter: Boolean = false)
        : Pair<DelugeActionsClient, DelugeConverter> {
        val delugeClient = mockk<DelugeActionsClient>()
        val converter = if (mockConverter) mockk() else DelugeConverter()

        every { delugeClient.torrents() } returns DelugeResult(
            result = TorrentsResult(
                mapOf("ee21ac410a4df9d2a09a97a6890fc74c0d143a0b" to delugeTorrentResponse)
            ),
            id = 8888,
            error = null
        )
        return delugeClient to converter
    }

}

private fun DefaultDelugeService.reflectExtractState(): DelugeState {
    return DefaultDelugeService::class.java.getDeclaredField("state").let {
        it.isAccessible = true
        return@let it.get(this)
    } as DelugeState
}