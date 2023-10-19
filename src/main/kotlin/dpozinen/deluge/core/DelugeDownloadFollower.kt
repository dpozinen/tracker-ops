package dpozinen.deluge.core

import dpozinen.deluge.kafka.StatsKafkaProducer
import dpozinen.deluge.rest.DelugeConverter
import dpozinen.deluge.rest.bytesToSize
import dpozinen.deluge.rest.clients.TorrentsResult.TorrentResult
import kotlinx.coroutines.delay
import mu.KotlinLogging.logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.Instant.now
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toKotlinDuration

@Service
class DelugeDownloadFollower(
    private val converter: DelugeConverter,
    private val producer: StatsKafkaProducer,
    private val callbacks: DownloadedCallbacks,
    @Value("\${tracker-ops.deluge.stats.follow.duration:4h}") private val followDuration: Duration,
    @Value("\${tracker-ops.deluge.stats.follow.initial-delay:20s}") private val initialDelay: Duration,
    private val delayProvider: DelayProvider
) {
    @Autowired
    constructor(
        converter: DelugeConverter,
        producer: StatsKafkaProducer,
        callbacks: DownloadedCallbacks,
        @Value("\${tracker-ops.deluge.stats.follow.duration:4h}") followDuration: String,
        @Value("\${tracker-ops.deluge.stats.follow.initial-delay:20s}") initialDelay: String,
        delayProvider: DelayProvider
    ) : this(
        converter, producer, callbacks, Duration.parse(followDuration), Duration.parse(initialDelay), delayProvider
    )

    private val log = logger {}


    suspend fun follow(torrent: TorrentResult, update: () -> List<TorrentResult>) {
        var followDelay = initialDelay
        val end = Instant.ofEpochMilli(now().toEpochMilli() + followDuration.inWholeMilliseconds)

        log.info("Will follow torrent ${torrent.name} with id ${torrent.id} for $followDuration until $end")
        while (true) {
            if (now().isAfter(end)) break

            delay(followDelay)

            runCatching {
                update()
                    .firstOrNull { it.id == torrent.id }
                    ?.also { victim ->
                        if (victim.state != "Downloading") {
                            val delay = calcMoveFileDelay(victim)
                            log.info { "Torrent ${victim.name} is done downloading, triggering scan jobs with $delay delay" }

                            delay(delay) // wait for deluge to move the torrent to 'done' folder
                            callbacks.trigger(victim, delay)

                            return@follow
                        } else {
                            followDelay = delayProvider.calculate(victim.eta)
                            log.debug { "${victim.name} is still downloading. Next poll in $followDelay \n $victim"  }

                            producer.send(converter.convert(victim))
                        }
                    } ?: log.error { "${torrent.name} is not found. What the fuck" }
            }.onFailure { log.error(it) { "Failed to follow ${torrent.name}" } }
        }
        log.info("It took over $followDuration for ${torrent.name} to complete, stopped following")
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

    @Component
    class DelayProvider(
        @Value("#{\${tracker-ops.deluge.stats.follow.datapoints-per-download}}")
        private val datapointsPerDownload: Map<java.time.Duration, Int>
    ) {
        fun calculate(eta: Double): Duration {
            return datapointsPerDownload.keys
                .sorted()
                .first { eta.seconds <= it.toKotlinDuration() }
                .let { eta / datapointsPerDownload[it]!! }.seconds
        }
    }
}

const val GIGABYTE: Long = 1_000_000_000
