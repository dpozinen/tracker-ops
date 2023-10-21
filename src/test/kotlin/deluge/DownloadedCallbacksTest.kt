package deluge

import Data.Companion.delugeTorrentResponse
import dpozinen.deluge.core.DownloadedCallbacks
import dpozinen.deluge.rest.DelugeRequest
import dpozinen.deluge.rest.DelugeRequest.Method.move_storage
import dpozinen.deluge.rest.clients.DelugeActionsClient
import dpozinen.deluge.rest.clients.DelugeResult
import dpozinen.deluge.rest.clients.PlexClient
import dpozinen.deluge.rest.clients.TrueNasClient
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.atIndex
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test
import kotlin.time.Duration.Companion.milliseconds


@ExtendWith(MockKExtension::class)
class DownloadedCallbacksTest {

    @RelaxedMockK
    private lateinit var trueNasClient: TrueNasClient
    @RelaxedMockK
    private lateinit var plexClient: PlexClient
    @RelaxedMockK
    private lateinit var delugeActionsClient: DelugeActionsClient

    @Test
    fun `trigger `() {
        val slot = slot<DelugeRequest>()
        every { delugeActionsClient.move(capture(slot)) } returns DelugeResult(true, null, 8888)

        val callbacks = DownloadedCallbacks(
            trueNasClient, plexClient, delugeActionsClient, "/show", "/film"
        )
        runBlocking {
            callbacks.trigger(delugeTorrentResponse, 2.milliseconds)
        }

        verifyOrder {
            trueNasClient.startCronJob()
            plexClient.scanLibrary(callbacks.showLibraryId)
            delugeActionsClient.move(any())
        }
        verify(exactly = 0) { plexClient.scanLibrary(callbacks.filmLibraryId) }

        assertThat(slot.captured).satisfies({
            assertThat(it.method).isEqualTo(move_storage)
            assertThat(it.params)
                .satisfies({ ids ->
                    assertThat(ids as Array<*>).containsExactly(delugeTorrentResponse.id)
                }, atIndex(0))
                .satisfies({ downloadFolder ->
                    assertThat(downloadFolder).isEqualTo("/show")
                }, atIndex(1))
        })
    }
}