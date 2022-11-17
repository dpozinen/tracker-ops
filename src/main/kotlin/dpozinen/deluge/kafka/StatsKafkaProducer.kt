package dpozinen.deluge.kafka

import dpozinen.deluge.domain.DataPoint
import mu.KotlinLogging
import org.springframework.kafka.core.KafkaTemplate


class StatsKafkaProducer(
    private val kafkaTemplate: KafkaTemplate<String, List<DataPoint>>,
    private val topic: String
) {

    private val log = KotlinLogging.logger {}

    fun send(stats: List<DataPoint>) {
        val future = kafkaTemplate.send(topic, stats)
        future.addCallback(
            { },
            { ex -> log.error(ex) { "Could not send stats" } }
        )
    }

}