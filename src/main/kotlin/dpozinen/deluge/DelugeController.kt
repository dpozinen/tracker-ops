package dpozinen.deluge

import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.web.bind.annotation.GetMapping

@RestController
class DelugeController(private val service: DelugeService) {
    private val log = KotlinLogging.logger {}

    @PostMapping("/deluge")
    fun addMagnet(@RequestBody magnet: String) {
        log.info("Received magnet for deluge {}", magnet)

        service.addMagnet(magnet)

        log.info("Magnet added to deluge")
    }

    @GetMapping("/deluge/torrents")
    fun delugeTorrents(): List<DelugeTorrent> {
        return service.torrents()
    }

    @SendTo("/continuous/torrents")
    fun delugeTorrentsContinuous(): List<DelugeTorrent> {
        return service.torrents()
    }

}