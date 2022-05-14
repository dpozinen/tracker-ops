package dpozinen

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@EnableScheduling
@SpringBootApplication
open class App

    fun main(args: Array<String>) {
        runApplication<App>(*args)
    }
