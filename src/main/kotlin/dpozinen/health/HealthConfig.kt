package dpozinen.health

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableScheduling
import org.springframework.web.client.RestClient

@Configuration
@EnableScheduling
open class HealthConfig {

    @Bean
    open fun restClient(
        @Value("\${tracker-ops.health.telegram.host}") host: String,
        @Value("\${tracker-ops.health.telegram.bot-segment}") botSegment: String,
    ): RestClient {
        return RestClient.builder()
            .baseUrl("$host/$botSegment")
            .build()
    }

}