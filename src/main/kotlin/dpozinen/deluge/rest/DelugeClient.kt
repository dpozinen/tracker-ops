package dpozinen.deluge.rest

import dpozinen.errors.DelugeClientException
import dpozinen.errors.DelugeServerDownException
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpHeaders.COOKIE
import org.springframework.http.HttpMethod.POST
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.RequestEntity.method
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.net.HttpCookie
import java.net.URI
import java.time.Duration

@Component
class DelugeClient(
    @Value("\${tracker-ops.deluge.host}") private val host: String,
    @Value("\${tracker-ops.deluge.port}") private val port: Int,
    private val rest: RestTemplate = RestTemplateBuilder()
        .setConnectTimeout(Duration.ofSeconds(5))
        .setReadTimeout(Duration.ofSeconds(5))
        .build(),
) {
    private val log = KotlinLogging.logger {}

    fun login() = send("auth.login", DelugeParams(listOf("deluge")), HttpCookie("dummy", ""))

    fun addMagnet(magnetParams: DelugeParams, session: HttpCookie) = send("core.add_torrent_magnet", magnetParams, session)

    fun torrents(torrentParams: DelugeParams, session: HttpCookie) = send("web.update_ui", torrentParams, session)

    fun connect(session: HttpCookie) {
        val response = send("web.get_hosts", DelugeParams.empty(), session)

        val hosts = response.body?.hosts() ?: listOf()
        if (hosts.isEmpty()) throw IllegalStateException("no hosts")

        send("web.connect", DelugeParams.connect(hosts[0].id), session)
    }

    private fun send(method: String, params: DelugeParams, session: HttpCookie): ResponseEntity<DelugeResponse> {
        if ("web.update_ui" != method) log.info("Sending {} to deluge", method)

        val response = try {
             rest.exchange<DelugeResponse>(
                 method(POST, URI("http://$host:$port/json"))
                     .contentType(APPLICATION_JSON)
                     .header(COOKIE, session.asHeader())
                     .body(body(method, params))
            )
        } catch (ex: ResourceAccessException) {
            throw DelugeServerDownException(ex)
        }

        if ("web.update_ui" != method) log.info("Received from deluge {}", response.body)

        response.body?.result ?: throw DelugeClientException(response.body)

        return response
    }

    private fun body(method: String, params: DelugeParams) = """
                        {
                            "method" : "$method",
                            "params" : $params,
                            "id" : 8888
                        }
                    """.trimIndent()

}

private fun HttpCookie.asHeader(): String {
    return "${this.name}=${this.value}"
}