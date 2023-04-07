package dpozinen.deluge.kafka

import dpozinen.deluge.domain.DataPoint
import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JsonSerializer


@Configuration
open class KafkaConfig {

    @Configuration
    @Profile("stats")
    @Import(KafkaAutoConfiguration::class)
    open class StatsKafkaConfig {

        @Bean
        open fun producerFactory(@Value("\${kafka.address}") kafkaAddress: String,
                                 @Value("\${kafka.blocked-timeout}") timeout: String,
        ): ProducerFactory<String, List<DataPoint>> =
            DefaultKafkaProducerFactory(
                mapOf<String, Any>(
                    ProducerConfig.BOOTSTRAP_SERVERS_CONFIG to kafkaAddress,
                    ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG to StringSerializer::class.java,
                    ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG to JsonSerializer::class.java,
                    ProducerConfig.MAX_BLOCK_MS_CONFIG to timeout
                )
            )

        @Bean
        open fun kafkaTemplate(producerFactory: ProducerFactory<String, List<DataPoint>>): KafkaTemplate<String, List<DataPoint>> =
            KafkaTemplate(producerFactory)

        @Bean
        open fun kafkaProducer(
            kafkaTemplate: KafkaTemplate<String, List<DataPoint>>,
            @Value("\${kafka.topic}") statsTopic: String
        ): StatsKafkaProducer = StatsKafkaProducer.DefaultStatsKafkaProducer(kafkaTemplate, statsTopic)

    }

    @Bean
    @ConditionalOnMissingBean
    open fun disabledKafkaProducer(): StatsKafkaProducer {
        return StatsKafkaProducer { }
    }
}