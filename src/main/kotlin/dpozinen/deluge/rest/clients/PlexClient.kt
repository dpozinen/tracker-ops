package dpozinen.deluge.rest.clients

import feign.*
import org.springframework.beans.factory.annotation.Value
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable


@FeignClient(
    name = "plex",
    url = "\${tracker-ops.plex.url}",
    configuration = [PlexClient.PlexClientConfig::class]
)
interface PlexClient {

    @GetMapping("/library/sections/{id}/refresh")
    @Headers("Content-Type: application/json", "Accept: */*")
    fun scanLibrary(@PathVariable id: Int)

    open class PlexClientConfig {

        @Bean
        open fun retryer() = Retryer.Default()

        @Bean
        open fun authHeader(
            @Value("\${tracker-ops.plex.api-key}") plexApiKeyPath: String
        ) = PlexAuthInterceptor(this::class.java.getResource(plexApiKeyPath)?.readText() ?: "")
    }

    class PlexAuthInterceptor(private val plexApiKey: String) : RequestInterceptor {

        private val plexToken = "X-Plex-Token"

        override fun apply(template: RequestTemplate) {
            template.query(plexToken, plexApiKey)
        }
    }
}