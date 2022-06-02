package dpozinen.deluge

import kotlinx.coroutines.*
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
@Profile("stats & !test")
open class StatUpdateJob(private val delugeStatsService: DelugeStatsService) {

    @Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
    @OptIn(ExperimentalTime::class, DelicateCoroutinesApi::class)
    @EventListener(value = [ApplicationReadyEvent::class])
    fun startJob() {
        GlobalScope.launch(Dispatchers.IO) {
            repeat(Int.MAX_VALUE) {
                delugeStatsService.updateStats()
                delay(Duration.minutes(5))
            }
        }
    }

}