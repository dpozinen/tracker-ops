package dpozinen.model

private const val separator =
    "--------------------------------------------------------------------\n"

class Torrents(val torrents: List<Torrent>) {

    override fun toString() =
        if (torrents.isEmpty()) "No Torrents"
        else torrents.withIndex()
            .joinToString(separator, separator) { "[${it.index}] ${it.value.name} \n" }

    companion object {
        fun empty() = Torrents(mutableListOf())
    }
}