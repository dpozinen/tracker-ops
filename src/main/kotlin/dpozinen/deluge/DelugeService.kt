package dpozinen.deluge

import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.net.HttpCookie
import java.util.concurrent.ConcurrentHashMap

@Service // todo 'web.connect'
class DelugeService(
    @Value("\${tracker-ops.manual-deluge.download-folder}") private val downloadFolder: String,
    private val delugeClient: DelugeClient
) {
    private val log = KotlinLogging.logger {}
    private var session: HttpCookie = HttpCookie.parse("dummy=dummy; max-age=0")[0]

    private var state: Set<String> = setOf()
    private val cache: MutableMap<Command, Set<String>> = ConcurrentHashMap()

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

    fun torrents(all: Boolean = false): List<DelugeTorrent> {
        login()
        val params = if (all) DelugeParams.torrents(setOf("ALL")) else DelugeParams.torrents(state)
        val response = delugeClient.torrents(params, session)
        return response.body.torrents()
    }

    fun mutate(command: Command) {
        val torrents = cache.computeIfAbsent(command) {
            command.perform(torrents(true)).map { it.id }.toSet()
        }

        synchronized(this) {
            this.state = torrents
        }
    }

}