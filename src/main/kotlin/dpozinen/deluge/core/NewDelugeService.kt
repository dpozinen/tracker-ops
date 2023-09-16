package dpozinen.deluge.core

import dpozinen.deluge.domain.DelugeTorrent
import dpozinen.deluge.domain.DelugeTorrents
import dpozinen.deluge.mutations.By
import dpozinen.deluge.mutations.Filter
import dpozinen.deluge.mutations.Mutation
import dpozinen.deluge.mutations.Sort
import dpozinen.deluge.rest.*
import kotlinx.coroutines.*
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Profile("!test")
@Service
class NewDelugeService(
    @Value("\${tracker-ops.deluge.download-folder}") private val downloadFolder: String,
    private val delugeClient: DelugeFeignClient,
    private val converter: DelugeConverter,
    private val follower: DelugeDownloadFollower
) : DelugeService {
    private val log = KotlinLogging.logger {}

    private var state: DelugeState = DelugeState().with(Sort(By.NAME), Filter(By.STATE, "Downloading"))

    override suspend fun addMagnet(magnet: String) {
        val oldTorrents = allTorrents()
        runBlocking {
            delugeClient.send(DelugeRequest.addMagnet(magnet, downloadFolder))
        }

        coroutineScope {
            delay(1000)
            allTorrents().toMutableList()
                .let { newTorrents ->
                    newTorrents.removeAll(oldTorrents)
                    newTorrents.firstOrNull()
                }
                ?.also {
                    CoroutineScope(Dispatchers.IO).launch {
                        follower.follow(it) { allTorrents() }
                    }
                    log.info { "Follow for ${it.name} triggered" }
                }
                ?: log.warn { "Could not launch torrent download tracking job" }
        }
    }

    override fun statefulTorrents(): DelugeTorrents {
        val torrents = allTorrents()
        val mutated = state.with(torrents).mutate().torrents

        return DelugeTorrents(mutated, info(torrents, mutated))
    }

    override fun allTorrents(): List<DelugeTorrent> {
        val response = delugeClient.send(DelugeRequest.torrents()).body!!

        return response.torrents().map { converter.convert(it) }
    }

    override fun mutate(mutation: Mutation) {
        synchronized(this) {
            state = state.mutate(mutation)
        }
        log.info("Mutated $mutation")
    }

}