package dpozinen

import dpozinen.deluge.rest.clients.*
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.kafka.autoconfigure.KafkaAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@ConfigurationPropertiesScan("dpozinen.music")
@EnableFeignClients(
    clients = [
        DelugeAuthClient::class,
        DelugeConnectionClient::class,
        DelugeActionsClient::class,
        PlexClient::class,
    ]
)
@SpringBootApplication(exclude = [KafkaAutoConfiguration::class])
open class App

fun main(args: Array<String>) {
    runApplication<App>(*args)
}
