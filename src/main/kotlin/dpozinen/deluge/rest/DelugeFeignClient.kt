package dpozinen.deluge.rest

import dpozinen.deluge.rest.DelugeRequest.Method.connect
import dpozinen.deluge.rest.DelugeRequest.Method.get_hosts
import dpozinen.errors.DelugeDisconnectedException
import dpozinen.errors.DelugeSessionExpiredException
import feign.*
import feign.codec.Decoder
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder
import org.springframework.context.annotation.Bean
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.io.IOException
import java.lang.reflect.Type
import kotlin.text.Charsets.UTF_8


@FeignClient(
    name = "deluge",
    url = "\${tracker-ops.deluge.host}:\${tracker-ops.deluge.port}",
    path = "/json",
    configuration = [DelugeFeignClient.DelugeClientConfig::class]
)
interface DelugeFeignClient {

    @PostMapping
    @Headers("Content-Type: application/json")
    fun send(@RequestBody body: DelugeRequest): ResponseEntity<DelugeResponse>

    class AuthRetryer(
        private val sessionHolder: DelugeSessionHolder,
        private val client: DelugeAuthClient
    ) : Retryer.Default() {
        override fun clone() = AuthRetryer(sessionHolder, client)

        override fun continueOrPropagate(e: RetryableException) {
            if (e is DelugeSessionExpiredException) {
                sessionHolder.refresh()
            } else if (e is DelugeDisconnectedException) {
                val hostId = client.hosts(DelugeRequest(get_hosts, DelugeParams.empty())).id()

                client.connect(DelugeRequest(connect, DelugeParams.connect(hostId)))
            }
            super.continueOrPropagate(e)
        }
    }

    class DelugeResponseDecoder : ResponseEntityDecoder(Decoder.Default()) {
        @Throws(IOException::class)
        override fun decode(response: Response, type: Type): Any {
            val body = response.body().asReader(UTF_8).readText()

            if (body.contains("Not authenticated")) {
                throw DelugeSessionExpiredException(response)
            } else if (body.contains("""connected": false""")) {
                throw DelugeDisconnectedException(response)
            }

            return super.decode(response, type)
        }
    }

    open class DelugeClientConfig {

        @Bean
        open fun retryer(sessionHolder: DelugeSessionHolder, client: DelugeAuthClient): Retryer =
            AuthRetryer(sessionHolder, client)

        @Bean
        open fun decoder() = DelugeResponseDecoder()

    }

}