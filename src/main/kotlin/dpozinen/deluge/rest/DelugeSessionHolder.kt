package dpozinen.deluge.rest

import dpozinen.deluge.rest.clients.DelugeAuthClient
import dpozinen.errors.DelugeServerDownException
import org.springframework.stereotype.Component
import org.springframework.web.client.ResourceAccessException
import java.net.HttpCookie


@Component
open class DelugeSessionHolder(private val client: DelugeAuthClient) {

    private val session: DelugeSession = DelugeSession()

    fun refresh() = get(true)

    fun get(force: Boolean = false): String {
        if (session.hasExpired() || force) {
            try {
                val response = client.login(DelugeRequest.login())
                val cookie = response.headers()["Set-Cookie"]?.first() ?: throw DelugeServerDownException()

                cookie.substringBefore("Expires")
                    .plus("max-age=3500") // HttpCookie can't parse the 'Expires' date format deluge sends...
                    .also { session.set(it) }
            } catch (ex: ResourceAccessException) {
                throw DelugeServerDownException(ex)
            }
        }
        return session.asHeader()
    }

    class DelugeSession {

        @Volatile
        private var cookie: HttpCookie = HttpCookie.parse("dummy=dummy; max-age=0")[0]

        fun asHeader() = "${cookie.name}=${cookie.value}"

        fun hasExpired() = cookie.hasExpired()

        fun set(cookie: String) {
            this.cookie = HttpCookie.parse(cookie)[0]
        }
    }
}

