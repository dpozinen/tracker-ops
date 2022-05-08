package dpozinen.model

import com.fasterxml.jackson.databind.annotation.JsonSerialize
import dpozinen.core.TorrentsDeserializer

private const val separator =
    "--------------------------------------------------------------------\n"

@JsonSerialize(using = TorrentsDeserializer::class)
class Torrents(val torrents: List<Torrent>) {

    override fun toString() =
        if (torrents.isEmpty()) "No Torrents"
        else torrents.withIndex()
            .joinToString(separator, separator) { "[${it.index}] ${it.value.name} \n" }



    companion object {
        fun empty() = Torrents(mutableListOf())
    }
}