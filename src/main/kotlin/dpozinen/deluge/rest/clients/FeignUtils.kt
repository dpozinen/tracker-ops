package dpozinen.deluge.rest.clients

import com.fasterxml.jackson.module.kotlin.KotlinModule
import dpozinen.deluge.rest.DelugeRequest
import dpozinen.deluge.rest.DelugeSessionHolder
import dpozinen.errors.DelugeDisconnectedException
import dpozinen.errors.DelugeSessionExpiredException
import feign.*
import feign.jackson.JacksonDecoder
import org.springframework.cloud.openfeign.support.ResponseEntityDecoder
import org.springframework.http.HttpHeaders
import java.io.IOException
import java.lang.reflect.Type
import kotlin.text.Charsets.UTF_8


class DelugeResponseDecoder : ResponseEntityDecoder(
    JacksonDecoder(listOf(KotlinModule.Builder().build()))
) {

    @Throws(IOException::class)
    override fun decode(response: Response, type: Type): Any {
        val body = response.body()
            .asReader(UTF_8)
            .readText()
            .replace("""result": null""", """result": true""")

        if (body.contains("Not authenticated")) {
            throw DelugeSessionExpiredException(response)
        } else if (body.contains("""connected": false""")) {
            throw DelugeDisconnectedException(response)
        }

        return super.decode(
            response.toBuilder().body(body, UTF_8).build(),
            type
        )
    }
}

class AuthRetryer(
    private val sessionHolder: DelugeSessionHolder,
    private val client: DelugeAuthClient
) : Retryer.Default() {

    override fun clone() = AuthRetryer(sessionHolder, client)

    override fun continueOrPropagate(e: RetryableException) {
        if (e is DelugeSessionExpiredException) sessionHolder.refresh()
        super.continueOrPropagate(e)
    }
}

class AuthConnectRetryer(
    private val authRetryer: AuthRetryer,
    private val connectionClient: DelugeConnectionClient
) : Retryer {

    override fun clone() = AuthConnectRetryer(authRetryer, connectionClient)

    override fun continueOrPropagate(e: RetryableException) {
        if (e is DelugeDisconnectedException) {
            val hostId = connectionClient.hosts().result.id()

            connectionClient.connect(DelugeRequest.connect(hostId))
        }
        authRetryer.continueOrPropagate(e)
    }
}

class DelugeAuthInterceptor(private val sessionHolder: DelugeSessionHolder) : RequestInterceptor {
    override fun apply(template: RequestTemplate) {
        template.header(HttpHeaders.COOKIE, sessionHolder.get())
    }
}