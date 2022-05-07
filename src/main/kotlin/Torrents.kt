private const val separator =
    "---------------------------------------------------------------------------------------------\n"

class Torrents(val torrents: List<Torrent>) {

    override fun toString() =
        torrents.withIndex()
            .joinToString(separator, separator) { "[${it.index}] ${it.value.name} --- " }

    companion object {
        fun empty() = Torrents(listOf())
    }
}