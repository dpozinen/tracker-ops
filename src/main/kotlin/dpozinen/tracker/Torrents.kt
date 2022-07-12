package dpozinen.tracker

import com.fasterxml.jackson.databind.annotation.JsonSerialize

private const val separator =
    "--------------------------------------------------------------------\n"

@JsonSerialize(using = Deserializers.TorrentsDeserializer::class)
class Torrents(val torrents: List<Torrent>) {

    override fun toString() =
        if (torrents.isEmpty()) "No Torrents"
        else torrents.withIndex()
            .joinToString(separator, separator) { "[${it.index}] ${it.value.name} \n" }



    companion object {
        fun empty() = Torrents(mutableListOf())
    }
}