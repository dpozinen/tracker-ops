package dpozinen.deluge

data class DelugeTorrents(val torrents: List<DelugeTorrent>, val stats: Stats) : AbstractList<DelugeTorrent>() {

    data class Stats(val selected: Int, val total: Int,
                     val paused: Int, val downloading: Int, val seeding: Int,
                     val activeDown: Int, val activeUp: Int,
                     val ratio: Double,
                     val downloaded: String, val uploaded: String
    )

    override val size: Int
        get() = torrents.size

    override fun get(index: Int) = torrents[index]
}