package dpozinen.deluge.rest

import dpozinen.deluge.DelugeService
import dpozinen.deluge.DelugeTorrent
import dpozinen.deluge.mutations.*
import dpozinen.errors.DelugeServerDownException
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.ReceiveChannel
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
    private var stream: Job? = null

    @PostMapping("/deluge")
    fun addMagnet(@RequestBody magnet: String) = service.addMagnet(magnet)

    @GetMapping("/deluge/torrents")
    fun delugeTorrents() = service.torrents()

    @MessageMapping("/stream/stop")
    fun streamStop() = runBlocking {
        if (stream?.isActive == true) {
            stream?.cancel()
            log.info("Closing torrent stream")
        }
    }

    @MessageMapping("/stream/commence")
    fun streamCommence() = runBlocking {
        streamStop()
        stream = launch {
            log.info("Commencing torrent stream")
            val channel = produceTorrents()
            channel.consumeEach { sendTorrents { it } }
        }
    }

    @MessageMapping("/stream/mutate/search")
    fun streamSearch(search: Search) = if (search.name.isEmpty()) mutateAndSend(Clear.AllSearches()) else mutateAndSend(search)

    @MessageMapping("/stream/mutate/clear")
    fun streamClear() = mutateAndSend(Clear())

    @MessageMapping("/stream/mutate/clear/search")
    fun streamClearSearch(search: Search) = mutateAndSend(Clear(search))

    @MessageMapping("/stream/mutate/clear/sort")
    fun streamClearSort(sort: Sort) = mutateAndSend(Clear(sort))

    @MessageMapping("/stream/mutate/sort")
    fun streamSort(sort: Sort) = mutateAndSend(sort)

    @MessageMapping("/stream/mutate/sort/reverse")
    fun streamSortReverse(sort: Sort) = mutateAndSend(Sort.Reverse(sort))

    @MessageMapping("/stream/mutate/filter")
    fun streamFilter(filter: Filter) = mutateAndSend(filter)

    @MessageMapping("/stream/mutate/filter/clear")
    fun streamClearFilter(filter: Filter) = mutateAndSend(Clear(filter))

    private fun mutateAndSend(mutation: Mutation) {
        service.mutate(mutation)
        sendTorrents()
    }

    private fun sendTorrents(torrents: () -> List<DelugeTorrent> = { service.torrents() }) {
       runCatching { template.convertAndSend("/topic/torrents", torrents.invoke()) }
           .onFailure {
               handleException(it)
           }
    }

    @Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceTorrents(): ReceiveChannel<List<DelugeTorrent>> = produce {
        repeat(900) {
            runCatching { service.torrents() }
                .onFailure { handleException(it) }
                .onSuccess { send(it) }
            delay(1000)
        }
        notifyAndStop(mapOf("msg" to "session timed out"))
    }

    private fun handleException(it: Throwable) {
        log.error("${it.message}. Cause: {}", it.cause?.message ?: "Not provided")
        val payload = when {
            it is DelugeServerDownException -> mapOf("err" to "Deluge server is down")
            it.message != null -> mapOf("err" to it.message!!)
            else -> mapOf("err" to "${it::class} error with no message")
        }
        notifyAndStop(payload)
    }

    private fun notifyAndStop(payload: Map<String, Any>) {
        template.convertAndSend("/topic/torrents/stop", payload)
        streamStop()
    }

}