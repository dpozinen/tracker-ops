package dpozinen.deluge.core

import dpozinen.deluge.domain.DelugeTorrent
import dpozinen.deluge.rest.clients.PlexClient
import dpozinen.deluge.rest.clients.TrueNasClient
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.stereotype.Service
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes


@Service
class DownloadedCallbacks(
    private val trueNasClient: TrueNasClient,
    private val plexClient: PlexClient
) {
    private val log = KotlinLogging.logger {}

    fun trueNasMove() {
        runCatching {
            trueNasClient.startCronJob()
        }.onFailure { log.error { it } }
            .onSuccess { log.info { "True nas move job succeeded" } }
    }

    fun plexScanLib() {
        fun scan(id: Int) = runCatching { plexClient.scanLibrary(id) }
            .onFailure { log.error { it } }
            .onSuccess { log.info { "Plex scan lib $id job succeeded" } }

        scan(1)
        scan(2)
    }

    suspend fun trigger(torrent: DelugeTorrent, delay: Duration = 2.minutes) {
        log.info("Triggering true nas move job")
        trueNasMove()

        log.info { "Waiting for $delay before triggering plex scan" }

        delay(delay)

        log.info("Triggering plex scan lib")
        plexScanLib()

        moveDownloadFolder(torrent)
    }

    private fun moveDownloadFolder(torrent: DelugeTorrent) {
        val regexS=".*S[0-9][0-9]?.*"
        val regexSeason=".*Season ?[0-9][0-9]?.*"

        val name = torrent.name.replace("FS88", "")
        if (name.matches(Regex(regexS)) || name.matches(Regex(regexSeason))) {

        }
    }
}