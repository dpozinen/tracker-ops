package dpozinen.translate

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import jakarta.servlet.http.HttpServletRequest
import kotlinx.coroutines.DelicateCoroutinesApi
import mu.KotlinLogging.logger
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping

@Controller
class PlexWebhookController(
//    private val translationService: TranslationService
) {
    private val log = logger {}
    private val objectMapper = jacksonObjectMapper()

    @OptIn(DelicateCoroutinesApi::class)
    @PostMapping("/api/callbacks/plex-webhook", consumes = [MULTIPART_FORM_DATA_VALUE])
    fun consume(parts: HttpServletRequest) {
        log.info { "Plex event: ${objectMapper.writeValueAsString(parts.parts.map { it.name })}" }
        log.info { "Plex payload: ${objectMapper.writeValueAsString(
            parts.parts.map { part -> part.inputStream.bufferedReader().use { it.readText() }  }
        )}" }
//        objectMapper.readValue(thumb, Map::class.java)
//        val event = thumb.bytes.contentToString()
//            .let {  }


//        event.takeIf { (it["event"] ?: "") == "library.new" }
//            ?.also { GlobalScope.launch {  translationService.process(it) } }
//            ?: log.debug { "Ignored event $event" }
    }

}