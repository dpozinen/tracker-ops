package dpozinen.deluge.domain

data class DelugeTorrents(val torrents: List<DelugeTorrent>, val info: Info) : Iterable<DelugeTorrent> {

    data class Info(val selected: Int, val total: Int,
                    val downloading: Int, val paused: Int, val seeding: Int,
                    val activeDown: Int, val activeUp: Int,
                    val downloaded: String, val ratio: Double, val uploaded: String,
                    val downSpeed: String, val upSpeed: String
    )

    override fun iterator() = torrents.iterator()
}