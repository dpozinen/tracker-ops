package dpozinen

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.boot.runApplication

@SpringBootApplication(exclude = [KafkaAutoConfiguration::class])
open class App

    fun main(args: Array<String>) {
        runApplication<App>(*args)
    }
