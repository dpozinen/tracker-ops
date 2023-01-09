package dpozinen.deluge.kafka

import dpozinen.deluge.domain.DataPoint
import org.apache.kafka.clients.producer.ProducerConfig.BOOTSTRAP_SERVERS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.MAX_BLOCK_MS_CONFIG
import org.apache.kafka.clients.producer.ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.*
import org.springframework.kafka.support.serializer.JsonSerializer


@EnableKafka
@Configuration
open class KafkaConfig {

    @Bean
    open fun producerFactory(@Value("\${kafka.address}") kafkaAddress: String,
                             @Value("\${kafka.blocked-timeout}") timeout: String,
    ): ProducerFactory<String, List<DataPoint>> =
        DefaultKafkaProducerFactory(
            mapOf<String, Any>(
                BOOTSTRAP_SERVERS_CONFIG to kafkaAddress,
                KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
                MAX_BLOCK_MS_CONFIG to timeout
            )
        )

    @Bean
    open fun kafkaTemplate(producerFactory: ProducerFactory<String, List<DataPoint>>): KafkaTemplate<String, List<DataPoint>> =
        KafkaTemplate(producerFactory)

    @Bean
    open fun kafkaProducer(
        kafkaTemplate: KafkaTemplate<String, List<DataPoint>>,
        @Value("\${kafka.topic}") statsTopic: String
    ) = StatsKafkaProducer(kafkaTemplate, statsTopic)
}