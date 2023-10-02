package dpozinen.deluge.rest

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import dpozinen.deluge.core.DownloadedCallbacks

@RestController
class CallbacksController(
    private val downloadedCallbacks: DownloadedCallbacks
) {
    @GetMapping("/api/callbacks/plex-scan")
    fun plexScanLibs() {
        downloadedCallbacks.plexScanLib()
    }

    @GetMapping("/api/callbacks/true-nas-move")
    fun trueNasMove() {
        downloadedCallbacks.trueNasMove()
    }
}