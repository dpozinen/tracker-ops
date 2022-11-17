package dpozinen.deluge.core

import dpozinen.deluge.db.MigrationRepository
import dpozinen.deluge.kafka.StatsKafkaProducer
import dpozinen.deluge.rest.DelugeConverter
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
@Profile("stats")
class DelugeStatsService(
    private val migrationRepository: MigrationRepository,
    private val delugeService: DelugeService,
    private val converter: DelugeConverter,
    private val producer: StatsKafkaProducer,
    @Value("\${tracker-ops.migrate-to-influx:false}")
    private val performMigration: Boolean
) {

    fun collectStats() {
        val stats = converter.convert(delugeService.allTorrents())

        producer.send(stats)
    }

    @EventListener
    fun migrateStatsToInflux() {
        if (performMigration) {
            for (i in 0..50_000) {
                val stats = migrationRepository.findAll(i * 500)
                if (stats.isEmpty()) break
                else producer.send(stats)
            }
        }
    }

}
