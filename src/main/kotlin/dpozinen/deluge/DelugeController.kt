package dpozinen.deluge

import dpozinen.errors.DelugeServerDownException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.client.ResourceAccessException

@RestController
class DelugeController(private val service: DelugeService,
                       private val template: SimpMessagingTemplate) {
    private val log = KotlinLogging.logger {}

    @PostMapping("/deluge")
    fun addMagnet(@RequestBody magnet: String) {
        catch {
            log.info("Received magnet for deluge {}", magnet)

            service.addMagnet(magnet)

            log.info("Magnet added to deluge")
        }
    }

    @GetMapping("/deluge/torrents")
    fun delugeTorrents(): List<DelugeTorrent> {
        return catch { service.torrents() }
    }

    @Scheduled(fixedRate = 1000)
    fun delugeTorrentsContinuous() {
        catch { template.convertAndSend("/topic/torrents", service.torrents()) }
    }

    @MessageMapping("/stream/search")
    fun delugeTorrentsContinuousSearch(search: Command.Search) {
        catch { service.mutate(search) }
    }

    private fun <R> catch(action : () -> R): R {
        try {
            return action.invoke()
        } catch (ex: ResourceAccessException) {
            throw DelugeServerDownException(ex)
        }
    }
}