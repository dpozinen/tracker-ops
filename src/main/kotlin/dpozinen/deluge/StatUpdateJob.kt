package dpozinen.deluge

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Component
@ConditionalOnProperty(name = ["track-stats"], prefix = "tracker-ops.deluge", havingValue = "true")
open class StatUpdateJob(private val delugeStatsService: DelugeStatsService) {

    @Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
    @OptIn(ExperimentalTime::class)
    @EventListener(value = [ApplicationReadyEvent::class])
    fun startJob() {
        runBlocking {
            launch(Dispatchers.IO) {
                repeat(Int.MAX_VALUE) {
                    delugeStatsService.updateStats()
                    delay(Duration.minutes(5))
                }
            }
        }
    }

}