package dpozinen.tracker

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.jackson.JsonComponent
import org.springframework.web.context.request.RequestContextHolder
import org.springframework.web.context.request.ServletRequestAttributes
import java.io.IOException
import javax.servlet.http.HttpServletRequest


@JsonComponent
class TorrentsDeserializer(@Value("\${tracker-ops.host:localhost}") private val address: String,
                           @Value("\${server.port:8133}") private val port: String,
) : JsonSerializer<Torrents>() {

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
            json.writeStartObject()

            json.writeNumberField("index", i)
            json.writeStringField("name", torrent.name)
            json.writeStringField("size", torrent.size)
            json.writeNumberField("seeds", torrent.seeds)
            json.writeNumberField("leeches", torrent.leeches)
            json.writeStringField("date", torrent.date)
            json.writeStringField("contributor", torrent.contributor)
            json.writeStringField("link", searchResultLink(request, i))
            json.writeEndObject()
        }
        json.writeEndArray()
    }

    private fun searchResultLink(request: HttpServletRequest, i: Int) =
        "${request.scheme}://${address}:${port}${request.requestURI}/select/${i}"

}