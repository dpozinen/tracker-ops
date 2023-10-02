package dpozinen.deluge.rest.clients

import com.fasterxml.jackson.annotation.JsonProperty
import feign.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@FeignClient(
    name = "truenas",
    url = "\${tracker-ops.truenas.url}",
    path = "/api/v2.0",
    configuration = [TrueNasClient.PlexClientConfig::class]
)
interface TrueNasClient {

    @PostMapping("/cronjob/run")
    @Headers("Content-Type: application/json", "Accept: */*")
    fun startCronJob(@RequestBody body: CronJobStartRequest = CronJobStartRequest())

    data class CronJobStartRequest(
        val id: Int = 1,
        @JsonProperty("skip_disabled") val skipDisabled: Boolean = false
    )

    open class PlexClientConfig {

        @Bean
        open fun retryer() = Retryer.Default()

        @Bean
        open fun authHeader(
            @Value("\${tracker-ops.truenas.api-key}") apiKey: String) = TrueNasAuthInterceptor(apiKey)
    }

    class TrueNasAuthInterceptor(private val apiKey: String) : RequestInterceptor {
        override fun apply(template: RequestTemplate) {
            template.header(AUTHORIZATION, "Bearer $apiKey".trim())
        }
    }
}