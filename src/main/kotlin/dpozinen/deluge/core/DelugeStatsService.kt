package dpozinen.deluge.core

import dpozinen.deluge.kafka.StatsKafkaProducer
import dpozinen.deluge.rest.DelugeConverter
import mu.KotlinLogging
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("stats")
class DelugeStatsService(
    private val delugeService: DelugeService,
    private val converter: DelugeConverter,
    private val producer: StatsKafkaProducer,
) {

    private val log = KotlinLogging.logger {}

    fun collectStats() {
        val stats = converter.convert(delugeService.allTorrents())

        try {
            producer.send(stats)
        } catch (e: Exception) {
            log.error(e) { "Could not send stats" }
        }
    }

}
