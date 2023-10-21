package dpozinen.deluge.core

import dpozinen.deluge.rest.DelugeRequest
import dpozinen.deluge.rest.clients.DelugeActionsClient
import dpozinen.deluge.rest.clients.PlexClient
import dpozinen.deluge.rest.clients.TorrentsResult.TorrentResult
import dpozinen.deluge.rest.clients.TorrentsResult.TorrentResult.TorrentType.FILM
import dpozinen.deluge.rest.clients.TorrentsResult.TorrentResult.TorrentType.SHOW
import dpozinen.deluge.rest.clients.TrueNasClient
import kotlinx.coroutines.delay
import mu.KotlinLogging.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes


@Service
class DownloadedCallbacks(
    private val trueNasClient: TrueNasClient,
    private val plexClient: PlexClient,
    private val delugeActionsClient: DelugeActionsClient,
    @Value("\${tracker-ops.deluge.folders.show}") private val showFolder: String,
    @Value("\${tracker-ops.deluge.folders.film}") private val filmFolder: String
) {
    private val log = logger {}

    val filmLibraryId: Int = 1
    val showLibraryId: Int = 2

    suspend fun trigger(torrent: TorrentResult, delay: Duration = 2.minutes) {
        log.info("Triggering true nas move job")
        trueNasMove()

        log.info { "Waiting for $delay before triggering plex scan" }
        delay(delay)

        log.info("Triggering plex scan lib")
        plexScanLib(
            when (torrent.type()) {
                SHOW -> showLibraryId; FILM -> filmLibraryId
            }
        )

        log.info("Triggering true nas move download folder")
        moveDownloadFolder(torrent)
    }

    fun trueNasMove() {
        runCatching {
            trueNasClient.startCronJob()
        }.onFailure { log.error { it } }
            .onSuccess { log.info { "True nas move job succeeded" } }
    }

    fun plexScanLib(vararg ids: Int) {
        ids.forEach { scan(it) }
    }

    private fun scan(id: Int) = runCatching { plexClient.scanLibrary(id) }
        .onFailure { log.error { it } }
        .onSuccess { log.info { "Plex scan lib $id job succeeded" } }

    fun moveDownloadFolder(vararg torrents: TorrentResult) =
        torrents.forEach { torrent ->
            when (torrent.type()) {
                SHOW -> moveTo(torrent, showFolder)
                FILM -> moveTo(torrent, filmFolder)
            }
        }

    private fun moveTo(torrent: TorrentResult, to: String) {
        log.info { "Moving ${torrent.name} to $to" }
        delugeActionsClient.move(DelugeRequest.move(to, torrent.id!!))
    }


}