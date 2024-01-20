package dpozinen.deluge.core

import kotlinx.coroutines.*
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component
import kotlin.time.Duration

@Component
@ConditionalOnProperty("tracker-ops.deluge.stats.enabled", havingValue = "true", matchIfMissing = true)
open class StatUpdateJob(
    private val delugeStatsService: DelugeStatsService,
    private val delugeService: DelugeService,
    @Value("\${tracker-ops.deluge.stats.poll-interval}") private val interval: String
) {
    private val log = KotlinLogging.logger { }

    @Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
    @OptIn(DelicateCoroutinesApi::class)
    @EventListener(value = [ApplicationReadyEvent::class])
    fun startJob() {
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                runCatching {
                    delugeStatsService.collectStats()
                    delugeService.followDownloading()
                }.onFailure {
                    log.error { it }
                }
                delay(Duration.parse(interval))
            }
        }
    }

}