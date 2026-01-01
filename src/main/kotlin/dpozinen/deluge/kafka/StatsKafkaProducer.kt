package dpozinen.deluge.kafka

import dpozinen.deluge.domain.DataPoint
import mu.KotlinLogging.logger
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.resilience.annotation.Retryable

fun interface StatsKafkaProducer {

    fun send(stats: List<DataPoint>)

    class DefaultStatsKafkaProducer(
        private val kafkaTemplate: KafkaTemplate<String, List<DataPoint>>
    ) : StatsKafkaProducer {

        private val log = logger {}

        @Retryable(
            maxRetriesString = "\${kafka.producer.retryCount:3}",
            delayString = "\${kafka.producer.retryDelayMillis:10000}",
            multiplierString = "\${kafka.producer.retryMultiplier:2}"
        )
        override fun send(stats: List<DataPoint>) {
            log.trace { "Sending stats about ${stats.map { it.name }.toSet()}" }
            kafkaTemplate.sendDefault(stats)
        }
    }

}