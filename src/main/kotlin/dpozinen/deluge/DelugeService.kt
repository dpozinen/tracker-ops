package dpozinen.deluge

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.HttpCookie

@Service // todo 'web.connect'
class DelugeService(
    @Value("\${tracker-ops.manual-deluge.download-folder}") private val downloadFolder: String,
    private val delugeClient: DelugeClient
) {
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

    fun addMagnet(magnet: String) {
        login()
        delugeClient.addMagnet(DelugeParams.addMagnet(magnet, downloadFolder), session)
    }

    fun torrents(): List<DelugeTorrent> {
        login()
        val params = DelugeParams.torrents(setOf("ALL"))
        val response = delugeClient.torrents(params, session)
        val torrents = response.body.torrents()
        return state.with(torrents).mutate().torrents
    }

    fun mutate(mutation: Mutation) {
        synchronized(this) {
            state = state.mutate(mutation)
        }
    }

}