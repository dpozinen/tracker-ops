package dpozinen.tracker

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jackson.JsonComponent
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.io.IOException
import javax.annotation.PostConstruct
import javax.servlet.http.HttpServletRequest


@JsonComponent
open class Deserializers(@Value("\${tracker-ops.host:localhost}") private val address: String,
                         @Value("\${server.port:8133}") private val port: String,
) {

    @PostConstruct
    fun fillCompanion() {
        Companion.address = address
        Companion.port = port
    }

    class TorrentsDeserializer : JsonSerializer<Torrents>() {

        @Throws(IOException::class)
        override fun serialize(
            torrents: Torrents,
            json: JsonGenerator,
            serializerProvider: SerializerProvider
        ) {
            val request = (RequestContextHolder.getRequestAttributes() as ServletRequestAttributes).request

            json.writeStartArray()
            for (i in torrents.torrents.indices) {
                val torrent = torrents.torrents[i]
                with(json) {
                    writeStartObject()

                    writeNumberField("index", i)
                    writeStringField("name", torrent.name)
                    writeStringField("size", torrent.size)
                    writeNumberField("seeds", torrent.seeds)
                    writeNumberField("leeches", torrent.leeches)
                    writeStringField("date", torrent.date)
                    writeStringField("contributor", torrent.contributor)
                    writeStringField("link", searchResultLink(request, i))
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
    }
}