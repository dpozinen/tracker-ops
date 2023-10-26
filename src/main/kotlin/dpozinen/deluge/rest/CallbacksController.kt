package dpozinen.deluge.rest

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import dpozinen.deluge.core.DownloadedCallbacks
import mu.KotlinLogging.logger
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class CallbacksController(
    private val downloadedCallbacks: DownloadedCallbacks
) {

    private val log = logger {}

    @GetMapping("/api/callbacks/plex-scan")
    fun plexScanLibs() {
        downloadedCallbacks.plexScanLib(
            downloadedCallbacks.filmLibraryId,
            downloadedCallbacks.showLibraryId
        )
    }

    @GetMapping("/api/callbacks/true-nas-move")
    fun trueNasMove() {
        downloadedCallbacks.trueNasMove()
    }

    @PostMapping("/api/callbacks/sonarr")
    fun sonarr(@RequestBody body: Any) {
        log.info { jacksonObjectMapper().writeValueAsString(body) }
    }
}