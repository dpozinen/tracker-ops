package deluge

import Data.Companion.delugeTorrentResponse
import dpozinen.deluge.core.DelugeService
import dpozinen.deluge.core.SonarrCallbacks
import dpozinen.deluge.domain.DownloadSonarrEvent
import dpozinen.deluge.domain.EpisodeFile
import dpozinen.deluge.domain.Series
import dpozinen.deluge.rest.DelugeRequest
import dpozinen.deluge.rest.DelugeRequest.Method.move_storage
import dpozinen.deluge.rest.clients.DelugeActionsClient
import dpozinen.deluge.rest.clients.DelugeResult
import io.mockk.CapturingSlot
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.atIndex
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.BeforeTest

@ExtendWith(MockKExtension::class)
class SonarrCallbacksTest {

    @RelaxedMockK
    private lateinit var delugeService: DelugeService

    @MockK
    private lateinit var delugeActionsClient: DelugeActionsClient

    private lateinit var sonarrCallbacks: SonarrCallbacks

    @BeforeTest
    fun setup() {
        sonarrCallbacks = SonarrCallbacks("/Downloads/done", "/Downloads", delugeService, delugeActionsClient)
    }

    @Test
    fun `should trigger follow on download started event`() {
        sonarrCallbacks.downloadStarted()

        verify { delugeService.followDownloading() }
    }

    @Test
    fun `should move completed series on download completed`() {
        every { delugeService.rawTorrents() } returns listOf(
            delugeTorrentResponse.copy(
                name = "Peaky.Blinders.S01.COMPLETE.720p.BluRay.x264-GalaxyTV[TGx]",
                label = "sonarr",
                downloadLocation = "/Downloads/done"
            )
        )
        val slot = slot<DelugeRequest>()
        every { delugeActionsClient.move(capture(slot)) } returns DelugeResult(true, null, 8888)

        sonarrCallbacks.downloadCompleted(
            DownloadSonarrEvent(
                Series("/Show/Peaky Blinders"),
                EpisodeFile(
                    relativePath = "Season 1/Peaky.Blinders.S01E01.720p.BluRay.x264-GalaxyTV.mkv",
                    path = "/Downloads/done/Peaky.Blinders.S01.COMPLETE.720p.BluRay.x264-GalaxyTV[TGx]/Peaky.Blinders.S01E01.720p.BluRay.x264-GalaxyTV.mkv"
                )
            )
        )

        assertThatTorrentWasMoved(slot)
    }

    @Test
    fun `should move completed episode on download completed`() {
        every { delugeService.rawTorrents() } returns listOf(
            delugeTorrentResponse.copy(
                name = "Peaky.Blinders.S01E01.720p.BluRay.x264-GalaxyTV.mkv",
                label = "sonarr",
                downloadLocation = "/Downloads/done"
            )
        )
        val slot = slot<DelugeRequest>()
        every { delugeActionsClient.move(capture(slot)) } returns DelugeResult(true, null, 8888)

        sonarrCallbacks.downloadCompleted(
            DownloadSonarrEvent(
                Series("/Show/Peaky Blinders"),
                EpisodeFile(
                    relativePath = "Season 1/Peaky.Blinders.S01E01.720p.BluRay.x264-GalaxyTV.mkv",
                    path = "/Downloads/done/Peaky.Blinders.S01E01.720p.BluRay.x264-GalaxyTV.mkv"
                )
            )
        )

        assertThatTorrentWasMoved(slot)
    }

    @Test
    fun `should ignore already moved files on download completed`() {
        every { delugeService.rawTorrents() } returns listOf(
            delugeTorrentResponse.copy(
                name = "Peaky.Blinders.S01E01.720p.BluRay.x264-GalaxyTV.mkv",
                label = "sonarr"
            )
        )

        sonarrCallbacks.downloadCompleted(
            DownloadSonarrEvent(
                Series("/Show/Peaky Blinders"),
                EpisodeFile(
                    relativePath = "Season 1/Peaky.Blinders.S01E01.720p.BluRay.x264-GalaxyTV.mkv",
                    path = "/Downloads/done/Peaky.Blinders.S01E01.720p.BluRay.x264-GalaxyTV.mkv"
                )
            )
        )

        verify(exactly = 0) { delugeActionsClient.move(any()) }
    }

    @Test
    fun `should ignore non sonar downloads on download completed`() {
        every { delugeService.rawTorrents() } returns listOf(
            delugeTorrentResponse.copy(
                id = "non-sonarr-id",
                name = "Peaky.Blinders.S01E01.720p.BluRay.x264-GalaxyTV.mkv",
                downloadLocation = "/Downloads/done"
            ),
            delugeTorrentResponse.copy(
                name = "Peaky.Blinders.S01E01.720p.BluRay.x264-GalaxyTV.mkv",
                label = "sonarr",
                downloadLocation = "/Downloads/done"
            )
        )
        val slot = slot<DelugeRequest>()
        every { delugeActionsClient.move(capture(slot)) } returns DelugeResult(true, null, 8888)

        sonarrCallbacks.downloadCompleted(
            DownloadSonarrEvent(
                Series("/Show/Peaky Blinders"),
                EpisodeFile(
                    relativePath = "Season 1/Peaky.Blinders.S01E01.720p.BluRay.x264-GalaxyTV.mkv",
                    path = "/Downloads/done/Peaky.Blinders.S01E01.720p.BluRay.x264-GalaxyTV.mkv"
                )
            )
        )

        assertThatTorrentWasMoved(slot)
    }

    private fun assertThatTorrentWasMoved(slot: CapturingSlot<DelugeRequest>) {
        verify { delugeActionsClient.move(any()) }
        assertThat(slot.captured).satisfies({
            assertThat(it.method).isEqualTo(move_storage)
            assertThat(it.params)
                .satisfies({ ids ->
                    assertThat(ids as Array<*>).containsExactly(delugeTorrentResponse.id)
                }, atIndex(0))
                .satisfies({ downloadFolder ->
                    assertThat(downloadFolder).isEqualTo("/Downloads/Show/Peaky Blinders/Season 1")
                }, atIndex(1))
        })
    }
}