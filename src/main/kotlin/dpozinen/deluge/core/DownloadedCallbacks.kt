@file:OptIn(ExperimentalTime::class)

package dpozinen.deluge.core

import dpozinen.deluge.domain.DelugeTorrent
import dpozinen.deluge.rest.round
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
import java.time.Duration
import javax.annotation.PostConstruct
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.Duration as Dur


@Service
class DownloadedCallbacks(
    @Value("\${tracker-ops.truenas.host}") private val trueNasHost: String,
    @Value("\${tracker-ops.truenas.api-key}") private val trueNasApiKeyPath: String,
    @Value("\${tracker-ops.plex.host}") private val plexHost: String,
    @Value("\${tracker-ops.plex.port}") private val plexPort: String,
    @Value("\${tracker-ops.plex.api-key}") private val plexApiKeyPath: String,
    @Value("\${tracker-ops.follow-duration}") private val followDuration: String,
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

    suspend fun trigger(delay: Dur = Dur.minutes(2)) {
        trueNasMove()
        log.info("Triggered true nas move job")

        delay(delay)

        log.info("Triggered plex scan lib")
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
                    .setConnectTimeout(Duration.ofSeconds(5))
                    .setReadTimeout(Duration.ofSeconds(5))
                    .defaultHeader("Content-Type", "application/json")
                    .defaultHeader("Accept", "*/*")
                    .requestFactory { it }
                    .build()
            }

    suspend fun follow(torrent: DelugeTorrent, update: () -> List<DelugeTorrent>) {
        val followFor = Dur.parse(followDuration)
        log.info("Will follow download of torrent ${torrent.name} with id ${torrent.id} for $followFor")

        repeat(followFor.toLong(DurationUnit.MINUTES).toInt()) {
            delay(Dur.minutes(1))
            if (update().any { it.id == torrent.id && it.state != "Downloading" }) {
                val delay = calcDelayBetweenTriggers(torrent)
                log.info { "Torrent ${torrent.name} is done downloading, triggering scan jobs with $delay delay" }

                trigger(delay)

                return@follow
            }
        }
        log.info("It took over $followFor for ${torrent.name} to complete, stopped following")
    }

    private fun calcDelayBetweenTriggers(torrent: DelugeTorrent): Dur {
        return when {
            torrent.size.contains("GiB") -> {
                val exactDelay = torrent.size.substringBefore(" ")
                    .toDouble()
                    .round(1)
                    .times(10) // it takes about 10 seconds per Gb
                Dur.seconds(exactDelay + (exactDelay / 2)) // + 50% overhead just in case
            }
            else -> Dur.seconds(30)
        }
    }

}