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
    override fun addMagnet(magnet: String) {
        val oldTorrents = rawTorrents()
        runBlocking {
            delugeClient.addMagnet(DelugeRequest.addMagnet(magnet, downloadFolder))
        }

        GlobalScope.launch {
            delay(1000)
            rawTorrents()
                .toMutableList()
                .let { newTorrents ->
                    newTorrents.removeAll(oldTorrents)
                    newTorrents.firstOrNull()
                }
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
        log.info("Mutated $mutation")
    }

}