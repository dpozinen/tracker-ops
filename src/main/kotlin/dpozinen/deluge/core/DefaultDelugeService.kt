package dpozinen.deluge.core

import dpozinen.deluge.domain.DelugeTorrent
import dpozinen.deluge.domain.DelugeTorrents
import dpozinen.deluge.mutations.By
import dpozinen.deluge.mutations.Mutation
import dpozinen.deluge.mutations.Sort
import dpozinen.deluge.rest.*
import dpozinen.deluge.rest.clients.DelugeActionsClient
import kotlinx.coroutines.*
import mu.KotlinLogging.logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service

@Service
class DefaultDelugeService(
    @Value("\${tracker-ops.deluge.folders.download}") private val downloadFolder: String,
    private val delugeClient: DelugeActionsClient,
    private val converter: DelugeConverter,
    private val follower: DelugeDownloadFollower
) : DelugeService {
    private val log = logger {}

    private var state: DelugeState = DelugeState().with(Sort(By.NAME))

    @OptIn(DelicateCoroutinesApi::class)
    @EventListener(ApplicationReadyEvent::class,
        condition = "@environment.getRequiredProperty('tracker-ops.deluge.stats.follow.resume-on-startup')")
    override fun followDownloading() {
        delugeClient.torrents().result.torrents()
            .filter { it.state == "Downloading" }
            .forEach {
                GlobalScope.launch(Dispatchers.IO) {
                    follower.follow(it) { rawTorrents() }
                }
            }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun addMagnet(magnet: String) {
        val id = delugeClient.addMagnet(DelugeRequest.addMagnet(magnet, downloadFolder)).result

        GlobalScope.launch {
            delay(5000)
            rawTorrents()
                .firstOrNull { it.id == id }
                ?.also {
                    CoroutineScope(Dispatchers.IO).launch {
                        follower.follow(it) { rawTorrents() }
                    }
                    log.info { "Follow for ${it.name} triggered" }
                }
                ?: log.warn { "Could not launch torrent download tracking job" }
        }
    }

    override fun statefulTorrents(): DelugeTorrents {
        val torrents = rawTorrents()
        val mutated = state.with(torrents).mutate().torrents

        return DelugeTorrents(converter.toDelugeTorrents(mutated), info(torrents, mutated))
    }

    override fun allTorrents(): List<DelugeTorrent> {
        val response = delugeClient.torrents()

        return response.result.torrents().let { converter.toDelugeTorrents(it) }
    }

    override fun rawTorrents() = delugeClient.torrents().result.torrents()

    override fun mutate(mutation: Mutation) {
        synchronized(this) {
            state = state.mutate(mutation)
        }
        log.debug { "Mutated $mutation" }
    }

}