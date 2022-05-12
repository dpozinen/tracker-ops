package dpozinen.deluge

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.*
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.net.HttpCookie
import java.net.URI
import java.util.concurrent.ThreadLocalRandom

@Component
class DelugeClient(
    @Value("\${tracker-ops.manual-deluge.address}") private val delugeAddress: String,
    private val rest: RestTemplate = RestTemplate(),
) {
    private val log = KotlinLogging.logger {}

    fun login() = send("auth.login", DelugeParams(listOf("deluge")), HttpCookie("", ""))

    fun addMagnet(magnetParams: DelugeParams, session: HttpCookie) = send("core.add_torrent_magnet", magnetParams, session)

    fun torrents(torrentParams: DelugeParams, session: HttpCookie) = send("web.update_ui", torrentParams, session)

    private fun send(method: String, params: DelugeParams, session: HttpCookie): ResponseEntity<DelugeResponse> {
        log.info("Sending {} to deluge", method)

        val response = rest.exchange<DelugeResponse>(
            RequestEntity.method(HttpMethod.POST, URI("http://$delugeAddress/json"))
                .contentType(MediaType.APPLICATION_JSON)
                .header(HttpHeaders.COOKIE, session.toString())
                .body(body(method, params))
        )

        log.info("Received from deluge {}", response.body)

        response.body.result ?: throw DelugeClientException(response.body)

        return response
    }

    private fun body(method: String, params: DelugeParams) = """
                        {
                            "method" : "$method",
                            "params" : $params,
                            "id" : ${ThreadLocalRandom.current().nextInt()}
                        }
                    """.trimIndent()

}