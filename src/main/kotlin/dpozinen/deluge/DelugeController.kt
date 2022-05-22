package dpozinen.deluge

import dpozinen.deluge.mutations.*
import dpozinen.errors.defaultDummyData
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
                       private val template: SimpMessagingTemplate,
                       private val validator: Validator) {
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
            channel.consumeEach { socketSend { it } }
        }
    }

    @MessageMapping("/stream/mutate/search")
    fun streamSearch(search: Search) = mutateAndSend(search)

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

    @MessageMapping("/stream/mutate/sort")
    fun streamSort(dto: Filter.Dto) {
        val (valid, filter) = validator.validate(dto)
        if (valid) mutateAndSend(filter!!)
    }

    private fun mutateAndSend(mutation: Mutation) {
        service.mutate(mutation)
        runCatching { socketSend() }
            .onFailure {
                log.error("${it.message}. Cause: {}", it.cause?.message ?: "No provided")
                socketSend { defaultDummyData() }
                streamStop()
            }
    }

    private fun socketSend(torrents: () -> Any = { service.torrents() }) {
        template.convertAndSend("/topic/torrents", torrents.invoke())
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun CoroutineScope.produceTorrents(): ReceiveChannel<List<DelugeTorrent>> = produce {
        repeat(900) {
            val torrents = service.torrents()
            send(torrents)
            delay(1000)
        }
    }

}