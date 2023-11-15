package dpozinen

import dpozinen.deluge.rest.clients.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients
import org.springframework.context.annotation.Bean
import org.springframework.web.filter.CommonsRequestLoggingFilter


@EnableFeignClients(
    clients = [
        DelugeAuthClient::class,
        DelugeConnectionClient::class,
        DelugeActionsClient::class,
        PlexClient::class,
        TrueNasClient::class
    ]
)
@SpringBootApplication(exclude = [KafkaAutoConfiguration::class])
open class App {
    @Bean
    open fun logFilter(): CommonsRequestLoggingFilter {
        val filter = CommonsRequestLoggingFilter()
        filter.setIncludeQueryString(true)
        filter.setIncludePayload(true)
        filter.setMaxPayloadLength(10000)
        filter.setIncludeHeaders(false)
        filter.setAfterMessagePrefix("REQUEST DATA: ")
        return filter
    }
}

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
