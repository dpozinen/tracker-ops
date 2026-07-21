package dpozinen.deluge.core

import org.springframework.stereotype.Component


@Component
class SonarrCallbacks(private val delugeService: DelugeService) {

    fun downloadStarted() {
        delugeService.followDownloading()
    }

}