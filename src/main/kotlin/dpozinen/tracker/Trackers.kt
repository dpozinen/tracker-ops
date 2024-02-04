package dpozinen.tracker

enum class Trackers {
    OneThreeThree, TorrentGalaxy, Trunk;

    companion object {
        fun from(name: String) = when (name) {
            "133" -> OneThreeThree
            "torrent-galaxy" -> TorrentGalaxy
            "trunk" -> Trunk
            else -> throw IllegalArgumentException()
        }
    }
}