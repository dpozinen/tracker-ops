package dpozinen.deluge

import dpozinen.deluge.mutations.Clear
import dpozinen.deluge.mutations.Search
import dpozinen.deluge.mutations.Sort
import dpozinen.errors.DelugeServerDownException
import dpozinen.errors.defaultDummyData
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.consume
import kotlinx.coroutines.channels.consumeEach
import kotlinx.coroutines.channels.produce
import mu.KotlinLogging
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

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
    fun streamStop() = runBlocking {
        if (launch?.isActive == true) {
            launch?.cancel()
            log.info("Closing torrent stream")
        }
    }

    @MessageMapping("/stream/commence")
    fun streamCommence() = runBlocking {
        streamStop()
        log.info("Commencing torrent stream")
        val channel = produceTorrents()
        channel.consumeEach {
            template.convertAndSend("/topic/torrents", it)
        }
    }

    @MessageMapping("/stream/mutate/search")
    fun streamSearch(search: Search) = catch { service.mutate(search) }

    @MessageMapping("/stream/mutate/clear")
    fun streamClear() = catch { service.mutate(Clear()) }

    @MessageMapping("/stream/mutate/clear/search")
    fun streamClearSearch(search: Search) = catch { service.mutate(Clear(search)) }

    @MessageMapping("/stream/mutate/clear/sort")
    fun streamClearSort(sort: Sort) = catch { service.mutate(Clear(sort)) }

    @MessageMapping("/stream/mutate/sort")
    fun streamSort(sort: Sort) = catch { service.mutate(sort) }

    @MessageMapping("/stream/mutate/sort/reverse")
    fun streamSortReverse(sort: Sort) = catch { service.mutate(Sort.Reverse(sort)) }

    private fun <R> catch(block: () -> R) = try {
        block.invoke()
    } catch (ex: DelugeServerDownException) {
        log.error("${ex.message}. Cause: {}", ex.cause?.message)
        template.convertAndSend("/topic/torrents", defaultDummyData())
        streamStop()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceTorrents(): ReceiveChannel<List<DelugeTorrent>> = produce {
        repeat(900) {
            val torrents = service.torrents()

            repeat(5) {
                delay(300)
                send(torrents)
                log.info { "After Send" }
            }
        }
    }

}