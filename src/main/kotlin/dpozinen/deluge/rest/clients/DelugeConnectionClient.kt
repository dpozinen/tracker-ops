package dpozinen.deluge.rest.clients

import dpozinen.deluge.rest.DelugeRequest
import dpozinen.deluge.rest.DelugeRequest.DelugeParams
import dpozinen.deluge.rest.DelugeRequest.Method.get_hosts
import dpozinen.deluge.rest.DelugeSessionHolder
import feign.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@FeignClient(
    name = "deluge-connect",
    url = "\${tracker-ops.deluge.url}",
    path = "/json",
    configuration = [DelugeConnectionClient.DelugeConnectionClientConfig::class]
)
interface DelugeConnectionClient {

    @PostMapping
    @Headers("Content-Type: application/json")
    fun hosts(
        @RequestBody body: DelugeRequest = DelugeRequest(get_hosts, DelugeParams())
    ): DelugeResult<HostsResult>

    @PostMapping
    @Headers("Content-Type: application/json")
    fun connect(@RequestBody body: DelugeRequest)

    open class DelugeConnectionClientConfig {

        @Bean
        open fun retryer(sessionHolder: DelugeSessionHolder, client: DelugeAuthClient): Retryer =
            AuthRetryer(sessionHolder, client)

        @Bean
        open fun decoder() = DelugeResponseDecoder()

        @Bean
        open fun authHeader(sessionHolder: DelugeSessionHolder) = DelugeAuthInterceptor(sessionHolder)
    }
}