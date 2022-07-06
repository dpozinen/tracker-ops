package dpozinen.deluge.core

import dpozinen.deluge.domain.DelugeTorrent
import dpozinen.deluge.domain.DelugeTorrents
import dpozinen.deluge.mutations.Mutation
import dpozinen.deluge.rest.DelugeClient
import dpozinen.deluge.rest.DelugeParams
import dpozinen.deluge.rest.DelugeConverter
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.net.HttpCookie

@Profile("!test")
@Service
class RealDelugeService(
    @Value("\${tracker-ops.deluge.download-folder}") private val downloadFolder: String,
    private val delugeClient: DelugeClient,
    private val converter: DelugeConverter,
    private val callbacks: DownloadedCallbacks
) : DelugeService {
    private val log = KotlinLogging.logger {}
    private var session: HttpCookie = HttpCookie.parse("dummy=dummy; max-age=0")[0]

    private var state: DelugeState = DelugeState()

    private fun login(force: Boolean = false) {
        if (session.hasExpired() || force) {
            log.info("Session expired, logging in")

            val setCookie = delugeClient.login()
                .headers["Set-Cookie"]!![0]
                .substringBefore("Expires")
                .plus("max-age=3500") // HttpCookie can't parse the 'Expires' date format deluge sends...

            session = HttpCookie.parse(setCookie)[0]
        }
    }

    override fun addMagnet(magnet: String) {
        login()
        val oldTorrents = allTorrents().toMutableList()
        delugeClient.addMagnet(DelugeParams.addMagnet(magnet, downloadFolder), session)
        runBlocking { delay(500) }
        val newTorrent = allTorrents().toMutableList().let { it.removeAll(oldTorrents); it }
        if (newTorrent.isNotEmpty()) {
            runBlocking {
                callbacks.follow(newTorrent[0]) { allTorrents() }
            }
        } else {
            log.warn { "could not launch torrent download tracking job" }
        }
    }

    override fun statefulTorrents(): DelugeTorrents {
        val torrents = allTorrents()
        val mutated = state.with(torrents).mutate().torrents

        return DelugeTorrents(mutated, info(torrents, mutated))
    }

    override fun allTorrents(): List<DelugeTorrent> {
        login()
        val params = DelugeParams.torrents()
        var response = delugeClient.torrents(params, session).body

        if (response.disconnected()) {
            delugeClient.connect(session)
            login(true)
            response = delugeClient.torrents(params, session).body
        }

        return response.torrents().map { converter.convert(it) }
    }

    override fun mutate(mutation: Mutation) {
        synchronized(this) {
            state = state.mutate(mutation)
        }
        log.info("Mutated $mutation")
    }

}