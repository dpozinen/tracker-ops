package dpozinen.deluge

data class DelugeTorrents(val torrents: List<DelugeTorrent>, val stats: Stats) : Iterable<DelugeTorrent> {

    data class Stats(val selected: Int, val total: Int,
                     val downloading: Int, val paused: Int, val seeding: Int,
                     val activeDown: Int, val activeUp: Int,
                     val downloaded: String, val ratio: Double, val uploaded: String,
                     val downSpeed: String, val upSpeed: String
    )

    override fun iterator() = torrents.iterator()
}