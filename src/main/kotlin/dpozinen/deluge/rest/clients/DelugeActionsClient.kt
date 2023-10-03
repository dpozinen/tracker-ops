package dpozinen.deluge.rest.clients

import dpozinen.deluge.rest.DelugeRequest
import dpozinen.deluge.rest.DelugeSessionHolder
import feign.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@FeignClient(
    name = "deluge",
    url = "\${tracker-ops.deluge.url}",
    path = "/json",
    configuration = [DelugeActionsClient.DelugeClientConfig::class]
)
interface DelugeActionsClient {

    @PostMapping
    @Headers("Content-Type: application/json")
    fun torrents(@RequestBody body: DelugeRequest = DelugeRequest.torrents()): DelugeResult<TorrentsResult>

    @PostMapping
    @Headers("Content-Type: application/json")
    fun move(@RequestBody body: DelugeRequest): DelugeResult<Boolean>

    @PostMapping
    @Headers("Content-Type: application/json")
    fun addMagnet(@RequestBody body: DelugeRequest): DelugeResult<String>

    open class DelugeClientConfig {

        @Bean
        open fun retryer(
            sessionHolder: DelugeSessionHolder, client: DelugeAuthClient,
            connectionClient: DelugeConnectionClient
        ) = AuthConnectRetryer(AuthRetryer(sessionHolder, client), connectionClient)

        @Bean
        open fun decoder() = DelugeResponseDecoder()

        @Bean
        open fun authHeader(sessionHolder: DelugeSessionHolder) = DelugeAuthInterceptor(sessionHolder)

    }

}