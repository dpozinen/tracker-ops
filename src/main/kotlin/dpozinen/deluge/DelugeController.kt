package dpozinen.deluge

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import mu.KotlinLogging

@RestController
@ConditionalOnProperty(value = ["tracker-ops.manual-deluge.enabled"], havingValue = "true")
class DelugeController(private val service: DelugeService) {
    private val log = KotlinLogging.logger {}

    @PostMapping("/deluge")
    fun deluge(@RequestBody magnet: String) {
        log.info("Received magnet for deluge {}", magnet)

        val session = service.login()

        service.addMagnet(session, magnet)

        log.info("Magnet added to deluge")
    }

}