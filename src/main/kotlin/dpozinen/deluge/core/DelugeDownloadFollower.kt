package dpozinen.deluge.core

import dpozinen.deluge.kafka.StatsKafkaProducer
import dpozinen.deluge.rest.DelugeConverter
import dpozinen.deluge.rest.bytesToSize
import dpozinen.deluge.rest.clients.TorrentsResult.TorrentResult
import kotlinx.coroutines.delay
import mu.KotlinLogging.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.Instant.now
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

@Service
class DelugeDownloadFollower(
    private val converter: DelugeConverter,
    private val producer: StatsKafkaProducer,
    private val callbacks: DownloadedCallbacks,
    @Value("\${tracker-ops.follow-duration:4h}") private val followDuration: String,
    @Value("\${tracker-ops.follow-interval:1m}") private val followInterval: String,
) {
    private val log = logger {}

    suspend fun follow(torrent: TorrentResult, update: () -> List<TorrentResult>) {
        val followFor = Duration.parse(followDuration)
        val followDelay = Duration.parse(followInterval)
        val end = Instant.ofEpochMilli(now().toEpochMilli() + followFor.inWholeMilliseconds)

        log.info("Will follow torrent ${torrent.name} with id ${torrent.id} for $followDuration until $end")
        while (true) {
            if (now().isAfter(end)) break

            delay(followDelay)

            runCatching {
                update().firstOrNull { it.id == torrent.id }
                    ?.also { victim ->
                        run {
                            if (victim.state != "Downloading") {
                                val delay = calcMoveFileDelay(victim)
                                log.info { "Torrent ${victim.name} is done downloading, triggering scan jobs with $delay delay" }

                                delay(delay) // wait for deluge to move the torrent to 'done' folder
                                callbacks.trigger(victim, delay)

                                return
                            } else {
                                log.debug { "${victim.name} is still downloading.\n $victim" }

                                producer.send(converter.convert(victim))
                            }
                        }
                    } ?: log.error { "${torrent.name} is not found. What the fuck" }
            }.onFailure { log.info { "Failed to follow ${torrent.name}" } }
        }
        log.info("It took over $followFor for ${torrent.name} to complete, stopped following")
    }

    private fun calcMoveFileDelay(torrent: TorrentResult): Duration {
        log.info { "Torrent ${torrent.name} is sized at ${bytesToSize(torrent.size)} downloaded" }
        return when {
            torrent.downloaded > GIGABYTE -> { // it takes about 10 seconds per Gb
                val timeToMove = (torrent.downloaded / GIGABYTE) * 10
                (timeToMove + (timeToMove / 2)).seconds
            }
            torrent.downloaded < 10 -> 5.seconds
            else -> 30.seconds
        }
    }
}

const val GIGABYTE: Long = 1_000_000_000
