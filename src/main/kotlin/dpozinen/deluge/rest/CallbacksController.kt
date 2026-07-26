package dpozinen.deluge.rest

import dpozinen.deluge.core.DownloadedCallbacks
import dpozinen.deluge.core.SonarrCallbacks
import dpozinen.deluge.domain.GrabSonarrEvent
import dpozinen.deluge.domain.SonarrEvent
import dpozinen.deluge.domain.TestSonarrEvent
import mu.KotlinLogging.logger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import tools.jackson.databind.json.JsonMapper

@RestController
class CallbacksController(
    private val downloadedCallbacks: DownloadedCallbacks,
    private val sonarrCallbacks: SonarrCallbacks
) {

    private val log = logger {}
    private val mapper = JsonMapper.builder().findAndAddModules().build()

    @GetMapping("/api/callbacks/plex-scan")
    fun plexScanLibs() {
        downloadedCallbacks.plexScanLib(
            downloadedCallbacks.filmLibraryId,
            downloadedCallbacks.showLibraryId
        )
    }

    @GetMapping("/api/callbacks/download-started")
    fun follow() {
        sonarrCallbacks.downloadStarted()
    }

    @GetMapping("/api/callbacks/true-nas-move")
    fun trueNasMove() {
        downloadedCallbacks.trueNasMove()
    }

    @PostMapping("/api/callbacks/sonarr")
    fun sonarr(@RequestBody event: SonarrEvent) {
        log.info { mapper.writeValueAsString(event) }
        when (event) {
            is GrabSonarrEvent -> sonarrCallbacks.downloadStarted()
            is TestSonarrEvent -> log.info { "Sonarr test event received" }
        }
    }
}