package dpozinen.deluge

data class DelugeTorrents(val torrents: List<DelugeTorrent>, val stats: Stats) : Iterable<DelugeTorrent> {

    data class Stats(val selected: Int, val total: Int,
                     val paused: Int, val downloading: Int, val seeding: Int,
                     val activeDown: Int, val activeUp: Int,
                     val ratio: Double,
                     val downloaded: String, val uploaded: String,
                     val downSpeed: String, val upSpeed: String
    )

    override fun iterator() = torrents.iterator()
}