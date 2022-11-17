package dpozinen.deluge.core

import dpozinen.deluge.db.MigrationRepository
import dpozinen.deluge.kafka.StatsKafkaProducer
import dpozinen.deluge.rest.DelugeConverter
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
@Profile("stats")
class DelugeStatsService(
    private val migrationRepository: MigrationRepository,
    private val delugeService: DelugeService,
    private val converter: DelugeConverter,
    private val producer: StatsKafkaProducer
) {

    fun collectStats() {
        val stats = converter.convert(delugeService.allTorrents())

        producer.send(stats)
    }

    fun migrateStatsToInflux() {
        for (i in 0..50_000) {
            val stats = migrationRepository.findAll(i * 500)
            if (stats.isEmpty()) break
            else producer.send(stats)
        }
    }

}
