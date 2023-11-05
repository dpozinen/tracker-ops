package dpozinen.translate

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import kotlinx.coroutines.DelicateCoroutinesApi
import mu.KotlinLogging.logger
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@Controller
class PlexWebhookController(
//    private val translationService: TranslationService
) {
    private val log = logger {}
    private val objectMapper = jacksonObjectMapper()

    @OptIn(DelicateCoroutinesApi::class)
    @PostMapping("/api/callbacks/plex-webhook")
    fun consume(@RequestBody thumb: Any) {
        log.info { "Plex event: ${objectMapper.writeValueAsString(thumb)}" }
//        objectMapper.readValue(thumb, Map::class.java)
//        val event = thumb.bytes.contentToString()
//            .let {  }


//        event.takeIf { (it["event"] ?: "") == "library.new" }
//            ?.also { GlobalScope.launch {  translationService.process(it) } }
//            ?: log.debug { "Ignored event $event" }
    }
}