package dpozinen

import dpozinen.deluge.rest.DelugeAuthClient
import dpozinen.deluge.rest.DelugeFeignClient
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.runApplication
import org.springframework.cloud.openfeign.EnableFeignClients

@EnableFeignClients(clients = [DelugeFeignClient::class, DelugeAuthClient::class])
@SpringBootApplication(exclude = [KafkaAutoConfiguration::class])
open class App

    fun main(args: Array<String>) {
        runApplication<App>(*args)
    }
