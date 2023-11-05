package dpozinen.translate

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import mu.KotlinLogging.logger
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@Controller
class PlexWebhookController(private val translationService: TranslationService) {
    private val log = logger {}
    private val objectMapper = jacksonObjectMapper()

    @OptIn(DelicateCoroutinesApi::class)
    @PostMapping("/api/callbacks/plex-webhook")
    fun consume(@RequestPart thumb: MultipartFile) {
        val event = thumb.bytes.contentToString()
            .let { objectMapper.readValue(it, Map::class.java) }

        log.info { "Plex event: ${objectMapper.writeValueAsString(event)}" }

        event.takeIf { (it["event"] ?: "") == "library.new" }
            ?.also { GlobalScope.launch {  translationService.process(it) } }
            ?: log.debug { "Ignored event $event" }
    }
}