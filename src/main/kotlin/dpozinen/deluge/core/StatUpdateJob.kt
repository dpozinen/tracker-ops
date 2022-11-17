package dpozinen.deluge.core

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
@Profile("(stats & !test) | job-test")
open class StatUpdateJob(
    private val delugeStatsService: DelugeStatsService,
    @Value("\${tracker-ops.deluge.stats.poll-interval}") private val interval: String
) {
    private val log = KotlinLogging.logger {  }

    @Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
    @OptIn(ExperimentalTime::class, DelicateCoroutinesApi::class)
    @EventListener(value = [ApplicationReadyEvent::class])
    fun startJob() {
        GlobalScope.launch(Dispatchers.IO) {
            repeat(Int.MAX_VALUE) {
                runCatching { delugeStatsService.collectStats() }
                    .onFailure {
                        log.error { it }
                    }
                delay(Duration.parse(interval))
            }
        }
    }

}