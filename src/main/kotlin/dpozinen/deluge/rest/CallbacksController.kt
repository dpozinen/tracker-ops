package dpozinen.deluge.rest

import dpozinen.deluge.core.DownloadedCallbacks
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class CallbacksController(
    private val downloadedCallbacks: DownloadedCallbacks
) {
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
}