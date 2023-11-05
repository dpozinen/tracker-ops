package dpozinen.translate

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import mu.KotlinLogging.logger
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestPart
import org.springframework.web.multipart.MultipartFile

@Controller
class PlexWebhookController(private val translationService: TranslationService) {
    private val log = logger {}
    private val objectMapper = jacksonObjectMapper()

    @PostMapping("/api/callbacks/plex-webhook")
    fun consume(@RequestPart body: MultipartFile) {
        val event = body.bytes.contentToString()
            .let { objectMapper.readValue(it, Map::class.java) }

        log.info { "Plex event: ${objectMapper.writeValueAsString(event)}" }

        event.takeIf { (it["event"] ?: "") == "library.new" }
            ?.also { translationService.process(it) }
            ?: log.debug { "Ignored event $event" }
    }
}