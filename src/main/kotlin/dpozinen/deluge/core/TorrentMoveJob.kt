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
@ConditionalOnProperty("tracker-ops.deluge.folders.cleanup.enabled", havingValue = "true", matchIfMissing = true)
open class TorrentMoveJob(
    private val delugeService: DelugeService,
    private val downloadedCallbacks: DownloadedCallbacks,
    @Value("\${tracker-ops.deluge.folders.cleanup.interval:24h}") private val interval: String,
    @Value("\${tracker-ops.deluge.folders.done}") private val doneFolder: String
) {
    private val log = KotlinLogging.logger { }

    @Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
    @OptIn(DelicateCoroutinesApi::class)
    @EventListener(value = [ApplicationReadyEvent::class])
    fun startJob() {
        GlobalScope.launch(Dispatchers.IO) {
            while (true) {
                runCatching {
                    delugeService.rawTorrents()
                        .filter { it.downloadLocation == doneFolder }
                        .filterNot { it.isSonarrManaged() }
                        .toTypedArray()
                        .let {
                            downloadedCallbacks.moveDownloadFolder(*it)
                        }
                }.onFailure {
                    log.error { it }
                }
                delay(Duration.parse(interval))
            }
        }
    }

}