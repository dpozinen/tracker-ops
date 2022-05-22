package dpozinen.deluge

import dpozinen.deluge.mutations.Mutation
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.net.HttpCookie

@Profile("!dev")
@Service // todo 'web.connect'
class RealDelugeService(
    @Value("\${tracker-ops.deluge.download-folder}") private val downloadFolder: String,
    private val delugeClient: DelugeClient
):DelugeService {
    private val log = KotlinLogging.logger {}
    private var session: HttpCookie = HttpCookie.parse("dummy=dummy; max-age=0")[0]

    private var state: DelugeState = DelugeState()

    private fun login() {
        if (session.hasExpired()) {
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
        delugeClient.addMagnet(DelugeParams.addMagnet(magnet, downloadFolder), session)
    }

    override fun torrents(): List<DelugeTorrent> {
        login()
        val params = DelugeParams.torrents()
        val response = delugeClient.torrents(params, session)
        val torrents = response.body.torrents().map { DelugeTorrentConverter(it).convert() }
        return state.with(torrents).mutate().torrents
    }

    override fun mutate(mutation: Mutation) {
        synchronized(this) {
            state = state.mutate(mutation)
        }
        log.info("Mutated {}", mutation)
    }

}