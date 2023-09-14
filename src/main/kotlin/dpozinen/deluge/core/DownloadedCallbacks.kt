package dpozinen.deluge.core

import kotlinx.coroutines.delay
import mu.KotlinLogging
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
import javax.annotation.PostConstruct
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.toJavaDuration


@Service
class DownloadedCallbacks(
    @Value("\${tracker-ops.truenas.host}") private val trueNasHost: String,
    @Value("\${tracker-ops.truenas.api-key}") private val trueNasApiKeyPath: String,
    @Value("\${tracker-ops.plex.host}") private val plexHost: String,
    @Value("\${tracker-ops.plex.port}") private val plexPort: String,
    @Value("\${tracker-ops.plex.api-key}") private val plexApiKeyPath: String,
) {
    private val log = KotlinLogging.logger {}

    private val rest: RestTemplate = restTemplate()

    private lateinit var trueNasApiKey: String
    private lateinit var plexApiKey: String

    @PostConstruct
    fun init() {
        this.plexApiKey = this::class.java.getResource(plexApiKeyPath)?.readText() ?: ""
        this.trueNasApiKey = this::class.java.getResource(trueNasApiKeyPath)?.readText() ?: ""
    }

    fun trueNasMove() {
        runCatching {
            rest.exchange<String>(
                method(HttpMethod.POST, URI("https://$trueNasHost/api/v2.0/cronjob/run"))
                    .headers {
                        it["Content-Type"] = "application/json"
                        it["Accept"] = "*/*"
                        it["Authorization"] = "Bearer $trueNasApiKey".trim()
                    }
                    .body("""{ "id": 1,  "skip_disabled": false }""")
            )
        }.onFailure { log.error { it } }
            .onSuccess { log.info { "True nas move job succeeded" } }
    }

    fun plexScanLib() {
        fun scan(id: Int) = runCatching {
            rest.exchange<Any>(
                get(URI("https://$plexHost:$plexPort/library/sections/$id/refresh?X-Plex-Token=$plexApiKey".trim())).build()
            )
        }.onFailure { log.error { it } }
            .onSuccess { log.info { "Plex scan lib $id job succeeded" } }

        scan(1)
        scan(2)
    }

    suspend fun trigger(delay: Duration = 2.minutes) {
        log.info("Triggering true nas move job")
        trueNasMove()

        log.info { "Waiting for $delay before triggering plex scan" }

        delay(delay)

        log.info("Triggering plex scan lib")
        plexScanLib()
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
                    .setConnectTimeout(5.seconds.toJavaDuration())
                    .setReadTimeout(5.seconds.toJavaDuration())
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("Accept", "*/*")
                    .requestFactory { it }
                    .build()
            }

}