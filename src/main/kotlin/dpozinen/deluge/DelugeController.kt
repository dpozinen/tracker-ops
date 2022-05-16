package dpozinen.deluge

import dpozinen.errors.DelugeServerDownException
import dpozinen.errors.defaultDummyData
import kotlinx.coroutines.*
import mu.KotlinLogging
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
    private val log = KotlinLogging.logger {}
    private var launch: Job? = null

    @PostMapping("/deluge")
    fun addMagnet(@RequestBody magnet: String) = catch { service.addMagnet(magnet) }

    @GetMapping("/deluge/torrents")
    fun delugeTorrents() = service.torrents()

    @MessageMapping("/stream/stop")
    fun delugeTorrentsStreamStop() {
        runBlocking { if (launch?.isActive == true) launch?.cancel() }
    }

    @MessageMapping("/stream/commence")
    fun delugeTorrentsStreamCommence() = runBlocking {
        delugeTorrentsStreamStop()
        launch = launch { streamTorrents() }
    }

    @MessageMapping("/stream/search")
    fun delugeTorrentsStreamSearch(search: Mutation.Search) = catch { service.mutate(search) }

    @MessageMapping("/stream/clear")
    fun delugeTorrentsStreamClear() = catch { service.mutate(Mutation.Clear()) }

    private suspend fun streamTorrents() =
        (0..900).forEach { _ ->
            delay(1000)
            catch { template.convertAndSend("/topic/torrents", service.torrents()) }
        }

    private fun <R> catch(block: () -> R) = try {
        block.invoke()
    } catch (ex: DelugeServerDownException) {
        log.error("${ex.message}. Cause: {}", ex.cause?.message)
        template.convertAndSend("/topic/torrents", defaultDummyData())
        delugeTorrentsStreamStop()
    }
}