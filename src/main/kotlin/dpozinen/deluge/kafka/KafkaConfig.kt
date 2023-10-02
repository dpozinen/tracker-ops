package dpozinen.deluge.kafka

import dpozinen.deluge.domain.DataPoint
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.autoconfigure.kafka.KafkaAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Import
import org.springframework.kafka.core.KafkaTemplate


@Configuration
open class KafkaConfig {

    @Configuration
    @ConditionalOnProperty("tracker-ops.deluge.stats.enabled", havingValue = "true", matchIfMissing = true)
    @Import(KafkaAutoConfiguration::class)
    open class StatsKafkaConfig {

        @Bean
        open fun kafkaProducer(
            kafkaTemplate: KafkaTemplate<String, List<DataPoint>>
        ): StatsKafkaProducer = StatsKafkaProducer.DefaultStatsKafkaProducer(kafkaTemplate)

    }

    @Bean
    @ConditionalOnMissingBean
    open fun disabledKafkaProducer(): StatsKafkaProducer {
        return StatsKafkaProducer { }
    }
}