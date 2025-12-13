package dpozinen.health.rest

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.http.HttpHeaders.CONTENT_TYPE
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.client.RestClient

@Service
class Telegram(private val client: RestClient) {

    val log: Logger = LoggerFactory.getLogger(javaClass)

    var offset: Long? = null

    val chatId = "211015066"
    val heart: String = "\uD83E\uDEC0"
    val healthMessage: String = "ﮩ٨ـﮩﮩ٨ـ\uD83E\uDEC0ﮩ٨ـﮩﮩ٨ـ"

    @Scheduled(cron = "\${tracker-ops.health.telegram.healthcheck.cron}")
    fun monitorHealthCheck() {
        getUpdates(offset).result
            .takeIf { it.isNotEmpty() }
            ?.also {
                offset = it.last().updateId + 1
            }?.find {
                it.message.text.contains("")
            }?.also { update ->
                log.info("Found health check message: {}", update)
                sendMessage(
                    chatId = chatId,
                    text = healthMessage
                )
            }
    }

    @Scheduled(cron = "\${tracker-ops.health.heartbeat.cron}")
    fun heartbeat() {
        sendMessage(
            chatId = chatId,
            text = healthMessage
        )
    }

    fun getUpdates(offset: Long?): GetUpdatesResponse {
        return client.get()
            .uri { builder ->
                builder.path("/getUpdates")
                    .also { offset?.let { builder.queryParam("offset", it) } }
                    .build()
            }
            .retrieve()
            .body(GetUpdatesResponse::class.java)!!
    }

    fun sendMessage(
        chatId: String,
        text: String,
        markdown: Boolean = false,
        hidePreview: Boolean = true,
    ) {
        runCatching {
            client.post().uri("/sendMessage")
                .body(requestBody(chatId, text, hidePreview, markdown))
                .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
                .retrieve().toBodilessEntity()
        }.onFailure {
            log.error("Failed to send message: $it")
        }
    }

    private fun requestBody(
        chatId: String,
        text: String,
        hidePreview: Boolean,
        markdown: Boolean
    ) =
        """{
                "chat_id": "$chatId",
                "text": "$text",
                "link_preview_options": {
                    "is_disabled": $hidePreview
                }
                ${if (markdown) ",\"parse_mode\": \"MarkdownV2\"" else ""}
            }""".trimIndent()


}
