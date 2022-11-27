package dpozinen.deluge.core

import dpozinen.deluge.db.MigrationRepository
import dpozinen.deluge.kafka.StatsKafkaProducer
import dpozinen.deluge.rest.DelugeConverter
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
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

    private val log = KotlinLogging.logger {}

    fun collectStats() {
        val stats = converter.convert(delugeService.allTorrents())

        try {
            producer.send(stats)
        } catch (e: Exception) {
            log.error(e) { "Could not send stats" }
        }
    }

    @EventListener(value = [ApplicationReadyEvent::class])
    fun migrateStatsToInflux() {
        if (performMigration) {
            for (i in 0..50_000) {
                val stats = migrationRepository.findAll(i * 2000)
                if (stats.isEmpty()) {
                    break
                } else {
                    log.info { "Sending ${stats.size} to kafka" }
                    producer.send(stats)
                }
            }
            log.info { "Migration complete" }
        }
    }

}
