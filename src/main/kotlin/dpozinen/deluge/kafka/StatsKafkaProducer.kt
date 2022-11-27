package dpozinen.deluge.kafka

import dpozinen.deluge.domain.DataPoint
import mu.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Retryable


class StatsKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, List<DataPoint>>,
    private val topic: String
) {

    private val log = KotlinLogging.logger {}

    @Retryable(
        maxAttemptsExpression = "\${kafka.producer.retryCount:3}",
        backoff = Backoff(
            delayExpression = "\${kafka.producer.retryDelayMillis:10000}",
            multiplierExpression = "\${kafka.producer.retryMultiplier:2}",
            random = true
        )
    )
    fun send(stats: List<DataPoint>) {
        log.debug { "Sending stats about ${stats.map { it.name }.toSet()}" }
        val future = kafkaTemplate.send(topic, stats)
        future.addCallback(
            { },
            { ex -> log.error(ex) { "Could not send stats" } }
        )
    }

}