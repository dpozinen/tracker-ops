package dpozinen.deluge.core

import dpozinen.deluge.domain.DelugeTorrent
import kotlinx.coroutines.delay
import org.apache.http.conn.ssl.SSLConnectionSocketFactory
import org.apache.http.impl.client.HttpClients
import org.apache.http.ssl.SSLContexts
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.web.client.RestTemplateBuilder
import org.springframework.http.HttpMethod
import org.springframework.http.RequestEntity.get
import org.springframework.http.RequestEntity.method
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.client.exchange
import java.net.URI
import java.time.Duration
import javax.annotation.PostConstruct
import kotlin.time.ExperimentalTime


@Service
class DownloadedCallbacks(
    @Value("\${tracker-ops.truenas.host}") private val trueNasHost: String,
    @Value("\${tracker-ops.truenas.api-key}") private val trueNasApiKeyPath: String,
    @Value("\${tracker-ops.plex.host}") private val plexHost: String,
    @Value("\${tracker-ops.plex.port}") private val plexPort: String,
    @Value("\${tracker-ops.plex.api-key}") private val plexApiKeyPath: String,
) {

    private val rest: RestTemplate = restTemplate()

    private lateinit var trueNasApiKey: String
    private lateinit var plexApiKey: String

    @PostConstruct
    fun init() {
        this.plexApiKey = this::class.java.getResource(plexApiKeyPath)?.readText() ?: ""
        this.trueNasApiKey = this::class.java.getResource(trueNasApiKeyPath)?.readText() ?: ""
    }

    fun trueNasMove() {
        rest.exchange<String>(
            method(HttpMethod.POST, URI("https://$trueNasHost/api/v2.0/cronjob/run"))
                .header("Authorization", "Bearer $trueNasApiKey")
                .body("""{ "id": 1,  "skip_disabled": false }""")
        )
    }

    fun plexScanLib() {
        fun scan(id: Int) {
            rest.exchange<Any>(
                get(URI("https://$plexHost:$plexPort/library/sections/$id/refresh?X-Plex-Token=$plexApiKey")).build()
            )
        }
        scan(1)
        scan(2)
    }

    private fun restTemplate() =
        SSLContexts.custom()
            .loadTrustMaterial(null) { _, _ -> true }
            .build()
            .let { SSLConnectionSocketFactory(it) { _, _ -> true } }
            .let { HttpClients.custom().setSSLSocketFactory(it).build() }
            .let { HttpComponentsClientHttpRequestFactory(it) }
            .let {
                RestTemplateBuilder()
                    .setConnectTimeout(Duration.ofSeconds(5))
                    .setReadTimeout(Duration.ofSeconds(5))
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("Accept", "*/*")
                    .requestFactory { it }
                    .build()
            }

    @OptIn(ExperimentalTime::class)
    suspend fun follow(torrent: DelugeTorrent, update: () -> List<DelugeTorrent>) =
        repeat(240) {
            delay(kotlin.time.Duration.minutes(1))
            if (update().any { it.id == torrent.id && it.state == "Downloaded" }) {
                trueNasMove()
                delay(kotlin.time.Duration.minutes(2))
                plexScanLib()
                return
            }
        }

}