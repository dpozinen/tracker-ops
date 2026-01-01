package dpozinen.tracker

import jakarta.annotation.PostConstruct
import jakarta.servlet.http.HttpServletRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jackson.JacksonComponent
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import tools.jackson.core.JsonGenerator
import tools.jackson.databind.SerializationContext
import tools.jackson.databind.ValueSerializer
import java.io.IOException


@JacksonComponent
open class Deserializers(@param:Value("\${tracker-ops.host:localhost}") private val address: String,
                         @param:Value("\${server.port:8133}") private val port: String,
) {

    @PostConstruct
    fun fillCompanion() {
        Companion.address = address
        Companion.port = port
    }

    class TorrentsDeserializer : ValueSerializer<Torrents>() {

        @Throws(IOException::class)
        override fun serialize(
            torrents: Torrents,
            json: JsonGenerator,
            ctxt: SerializationContext
        ) {
            val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request

            json.writeStartArray()
            val sortedTorrents = torrents.torrents.sortedWith(qxrFirstComparator)
            for (i in sortedTorrents.indices) {
                val torrent = sortedTorrents[i]
                with(json) {
                    writeStartObject()

                    writeNumberProperty("index", i)
                    writeStringProperty("name", torrent.name)
                    writeStringProperty("size", torrent.size)
                    writeNumberProperty("seeds", torrent.seeds)
                    writeNumberProperty("leeches", torrent.leeches)
                    writeStringProperty("date", torrent.date)
                    writeStringProperty("contributor", torrent.contributor)
                    writeStringProperty("link", searchResultLink(request, torrents.torrents.indexOf(torrent)))
                    writeEndObject()
                }
            }
            json.writeEndArray()
        }

        private fun searchResultLink(request: HttpServletRequest, i: Int) =
            "${request.scheme}://${address}:${port}${request.requestURI}/select/${i}"
    }

    companion object {
        lateinit var address: String
        lateinit var port: String
        val qxrFirstComparator = Comparator<Torrent> { a, b ->
            val aContainsQxr = a.contributor.lowercase().trim().contains("qxr")
            val bContainsQxr = b.contributor.lowercase().trim().contains("qxr")

            when {
                // Both contain qxr - compare alphabetically
                aContainsQxr && bContainsQxr -> a.contributor.compareTo(b.contributor, ignoreCase = true)
                // Only a contains qxr - a comes first
                aContainsQxr -> -1
                // Only b contains qxr - b comes first
                bContainsQxr -> 1
                // Neither contains qxr - handle empty/non-empty and alphabetical
                a.contributor.isEmpty() && b.contributor.isEmpty() -> 0
                a.contributor.isEmpty() -> 1
                b.contributor.isEmpty() -> -1
                else -> a.contributor.compareTo(b.contributor, ignoreCase = true)
            }
        }
    }
}