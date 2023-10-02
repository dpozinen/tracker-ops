package dpozinen.deluge.core

import dpozinen.deluge.rest.DelugeRequest
import dpozinen.deluge.rest.clients.DelugeActionsClient
import dpozinen.deluge.rest.clients.PlexClient
import dpozinen.deluge.rest.clients.TorrentsResult.TorrentResult
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

    suspend fun trigger(torrent: TorrentResult, delay: Duration = 2.minutes) {
        log.info("Triggering true nas move job")
        trueNasMove()

        log.info { "Waiting for $delay before triggering plex scan" }
        delay(delay)

        log.info("Triggering plex scan lib")
        plexScanLib()

        log.info("Triggering true nas move download folder")
        moveDownloadFolder(torrent)
    }

    private fun trueNasMove() {
        runCatching {
            trueNasClient.startCronJob()
        }.onFailure { log.error { it } }
            .onSuccess { log.info { "True nas move job succeeded" } }
    }

    private fun plexScanLib() {
        fun scan(id: Int) = runCatching { plexClient.scanLibrary(id) }
            .onFailure { log.error { it } }
            .onSuccess { log.info { "Plex scan lib $id job succeeded" } }

        scan(1)
        scan(2)
    }

    private fun moveDownloadFolder(torrent: TorrentResult) =
        when (TorrentType.from(torrent.name)) {
            TorrentType.SHOW -> moveTo(torrent, showFolder)
            TorrentType.FILM -> moveTo(torrent, filmFolder)
        }

    private fun moveTo(torrent: TorrentResult, to: String) {
        delugeActionsClient.move(DelugeRequest.move(to, torrent.id!!))
    }

    private enum class TorrentType {
        SHOW, FILM;
        companion object {
            private val regexS = Regex(".*S[0-9][0-9]?.*")
            private val regexSeason = Regex(".*Season ?[0-9][0-9]?.*")
            fun from(name: String) =
                if (name.replace("FS88", "") matches(regexS) ||
                    name.replace("FS88", "") matches (regexSeason)) {
                    SHOW
                } else FILM
        }
    }
}