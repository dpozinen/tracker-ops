package dpozinen.core

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import dpozinen.model.Torrents
import java.io.IOException


class TorrentsDeserializer : JsonSerializer<Torrents>() {

    @Throws(IOException::class)
    override fun serialize(
        torrents: Torrents,
        json: JsonGenerator,
        serializerProvider: SerializerProvider
    ) {
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

            json.writeEndObject()
        }
        json.writeEndArray()
    }

}