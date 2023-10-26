package dpozinen.deluge.core

import org.springframework.stereotype.Service


@Service
class SonarrCallbacks(
    private val downloadedCallbacks: DownloadedCallbacks
) {


    fun downloadStarted() {
        // TODO
    }

    fun downloadCompleted() {
        // TODO
    }


}