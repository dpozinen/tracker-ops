package dpozinen.deluge.kafka

import dpozinen.deluge.domain.DataPoint
import mu.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable

fun interface StatsKafkaProducer {

    fun send(stats: List<DataPoint>)

    class DefaultStatsKafkaProducer(
        private val kafkaTemplate: KafkaTemplate<String, List<DataPoint>>
    ) : StatsKafkaProducer {

        private val log = KotlinLogging.logger {}

        @Retryable(
            maxAttemptsExpression = "\${kafka.producer.retryCount:3}",
            backoff = Backoff(
                delayExpression = "\${kafka.producer.retryDelayMillis:10000}",
                multiplierExpression = "\${kafka.producer.retryMultiplier:2}",
                random = true
            )
        )
        override fun send(stats: List<DataPoint>) {
            log.trace { "Sending stats about ${stats.map { it.name }.toSet()}" }
            val future = kafkaTemplate.send(kafkaTemplate.defaultTopic, stats)
            future.addCallback(
                { },
                { ex -> log.error(ex) { "Could not send stats" } }
            )
        }
    }

}