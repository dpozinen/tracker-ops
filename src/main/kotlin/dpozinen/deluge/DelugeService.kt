package dpozinen.deluge

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.POST
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.net.URI

@Service
@ConditionalOnProperty(value = ["tracker-ops.manual-deluge.enabled"], havingValue = "true")
class DelugeService(
    private val rest: RestTemplate = RestTemplate(),
    @Value("\${tracker-ops.manual-deluge.address}") private val delugeAddress: String,
    @Value("\${tracker-ops.manual-deluge.download-folder}") private val downloadFolder: String
) {
    private val log = KotlinLogging.logger {}

    fun login() = sendToDeluge("auth.login", """["deluge"]""")
        .headers["Set-Cookie"]!![0]
        .substringBefore(";")

    fun addMagnet(session: String, magnet: String) {
        sendToDeluge(
            "core.add_torrent_magnet",
            magnetParams(magnet),
            session
        )
    }

    private fun sendToDeluge(method: String, params: String, session: String = ""): ResponseEntity<DelugeResponse> {
        log.info("Sending {} to deluge", method)

        val response = rest.exchange<DelugeResponse>(
            RequestEntity(
                body(method, params),
                headers(session), POST,
                URI("http://$delugeAddress/json")
            )
        )

        log.info("Received from deluge {}", response.body)

        response.body.result ?: throw DelugeClientException(response.body)
        if (!response.body.result!!) throw DelugeClientException(response.body)

        return response
    }

    private fun headers(session: String): HttpHeaders {
        val headers = HttpHeaders()
        headers["Content-Type"] = listOf("application/json")
        headers["Cookie"] = listOf(session)
        return headers
    }

    private fun body(method: String, params: String) = """
                        {
                            "method" : "$method",
                            "params" : $params,
                            "id" : 109384
                        }
                    """.trimIndent()

    private fun magnetParams(magnet: String) = """ 
                    [
                        "$magnet",
                        { "download_location" : "$downloadFolder" }
                    ] 
                """.trimIndent()
}