package dpozinen.deluge

import dpozinen.errors.DelugeServerDownException
import kotlinx.coroutines.*
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.ResourceAccessException

@RestController
class DelugeController(private val service: DelugeService,
                       private val template: SimpMessagingTemplate) {

    private var launch: Job? = null

    @PostMapping("/deluge")
    fun addMagnet(@RequestBody magnet: String) = catch { service.addMagnet(magnet) }

    @GetMapping("/deluge/torrents")
    fun delugeTorrents() = catch { service.torrents() }

    @MessageMapping("/stream/stop")
    fun delugeTorrentsStreamStop() = runBlocking { launch?.cancelAndJoin() }

    @MessageMapping("/stream/commence")
    fun delugeTorrentsStreamCommence() = runBlocking { launch = launch { streamTorrents() } }

    @MessageMapping("/stream/search")
    fun delugeTorrentsStreamSearch(search: Command.Search) = catch { service.mutate(search) }

    @MessageMapping("/stream/clear")
    fun delugeTorrentsStreamClear() = catch { service.mutate(Command.Clear()) }

    private suspend fun streamTorrents() =
        (0..900).forEach { _ ->
            delay(1000)
            catch { template.convertAndSend("/topic/torrents", service.torrents()) }
        }

    private fun <R> catch(block: () -> R): R = try {
        block.invoke()
    } catch (ex: ResourceAccessException) {
        throw DelugeServerDownException(ex)
    }
}