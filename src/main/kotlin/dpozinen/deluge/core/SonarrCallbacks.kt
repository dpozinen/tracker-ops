package dpozinen.deluge.core

import dpozinen.deluge.domain.DownloadSonarrEvent
import dpozinen.deluge.rest.DelugeRequest
import dpozinen.deluge.rest.clients.DelugeActionsClient
import mu.KotlinLogging.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component


@Component
class SonarrCallbacks(
    @Value("\${tracker-ops.deluge.folders.done}") private val doneFolder: String,
    @Value("\${tracker-ops.deluge.folders.root}") private val rootFolder: String,
    private val delugeService: DelugeService,
    private val delugeActionsClient: DelugeActionsClient
) {
    private val log = logger {}

    fun downloadStarted() {
        delugeService.followDownloading()
    }

    fun downloadCompleted(event: DownloadSonarrEvent) {
        val path = event.episodeFile.path.removePrefix("$doneFolder/")
        val seasonFolder = event.episodeFile.relativePath.substringBefore("/")

        val torrentName = path.substringBefore("/")

        delugeService.rawTorrents()
            .first { it.isSonarrManaged() && it.name == torrentName }
            .also {
                log.info { "Matched ${event.episodeFile.path} with ${it.name}" }
                if (it.downloadLocation != doneFolder) {
                    log.info { "${it.name} is already moved to ${it.downloadLocation}" }
                } else {
                    delugeActionsClient.move(
                        DelugeRequest.move(
                            to = "$rootFolder${event.series.path}/$seasonFolder",
                            it.id!!
                        )
                    )
                }
            }
    }

}