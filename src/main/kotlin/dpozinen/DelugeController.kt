package dpozinen

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod.POST
import org.springframework.http.RequestEntity
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.net.URI

@RestController
@ConditionalOnProperty(value = ["tracker-ops.manual-deluge.enabled"], havingValue = "true")
class DelugeController(
    private val rest: RestTemplate = RestTemplate(),
    @Value("\${tracker-ops.manual-deluge.address}") private val delugeAddress: String,
    @Value("\${tracker-ops.manual-deluge.download-folder}") private val downloadFolder: String
) {

    @PostMapping("/deluge")
    fun deluge(@RequestBody magnet: String) {
        val response = sendToDeluge("auth.login", """["deluge"]""")

        val session = response.headers["Set-Cookie"]!![0].substringBefore(";")

        sendToDeluge("core.add_torrent_magnet",
            magnetParams(magnet),
            session
        )
    }

    private fun sendToDeluge(method: String, params: String, session: String = ""): ResponseEntity<String> {
        val headers = HttpHeaders()
        headers["Content-Type"] = listOf("application/json")
        headers["Cookie"] = listOf(session)

        return rest.exchange(
            RequestEntity(
                body(method, params),
                headers, POST,
                URI("http://$delugeAddress/json")
            )
        )
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