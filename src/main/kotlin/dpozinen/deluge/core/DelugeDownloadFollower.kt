@file:OptIn(ExperimentalTime::class)

package dpozinen.deluge.core

import dpozinen.deluge.domain.DelugeTorrent
import dpozinen.deluge.kafka.StatsKafkaProducer
import dpozinen.deluge.rest.DelugeConverter
import dpozinen.deluge.rest.round
import kotlinx.coroutines.delay
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.Instant.now
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Service
class DelugeDownloadFollower(
    private val converter: DelugeConverter,
    private val producer: StatsKafkaProducer,
    private val callbacks: DownloadedCallbacks,
    @Value("\${tracker-ops.follow-duration:4h}") private val followDuration: String,
    @Value("\${tracker-ops.follow-interval:1m}") private val followInterval: String,
) {
    private val log = KotlinLogging.logger {}

    suspend fun follow(torrent: DelugeTorrent, update: () -> List<DelugeTorrent>) {
        val followFor = Duration.parse(followDuration)
        val followDelay = Duration.parse(followInterval)

        val end = Instant.ofEpochMilli(now().toEpochMilli() + followFor.inWholeMilliseconds)

        log.info("Will follow download of torrent ${torrent.name} with id ${torrent.id} for $followFor until $end")
        repeat(Int.MAX_VALUE) {
            if (now().isAfter(end)) return@repeat

            delay(followDelay)

            runCatching {
                val torrents = update()
                val victim = torrents.firstOrNull { it.id == torrent.id }

                if (victim == null) {
                    log.debug { "${torrent.name} is not found. What the fuck. Here's what was found: $torrents" }
                } else if (victim.state != "Downloading") {
                    val delay = calcDelayBetweenTriggers(torrent)
                    log.info { "Torrent ${torrent.name} is done downloading, triggering scan jobs with $delay delay" }

                    delay(delay) // wait for deluge to move the torrent to 'done' folder
                    callbacks.trigger(delay)

                    return@follow
                } else {
                    log.debug { "${torrent.name} is still downloading.\n $victim" }

                    val stats = converter.convert(listOf(victim))
                    producer.send(stats)
                }
            }.onFailure { log.info { "Failed to follow ${torrent.name}" } }
        }
        log.info("It took over $followFor for ${torrent.name} to complete, stopped following")
    }

    private fun calcDelayBetweenTriggers(torrent: DelugeTorrent): Duration {
        log.info { "Torrent ${torrent.name} is sized at ${torrent.size} downloaded is ${torrent.downloaded}" }
        return when {
            torrent.downloaded.contains("GiB") -> {
                val exactDelay = torrent.downloaded.substringBefore(" ")
                    .toDouble()
                    .round(1)
                    .times(10) // it takes about 10 seconds per Gb
                Duration.seconds(exactDelay + (exactDelay / 2)) // + 50% overhead just in case
            }
            else -> Duration.seconds(30)
        }
    }
}