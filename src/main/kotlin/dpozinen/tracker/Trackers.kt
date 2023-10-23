package dpozinen.tracker

enum class Trackers {
    OneThreeThree, TorrentGalaxy;

    companion object {
        fun from(name: String) = when (name) {
            "133" -> OneThreeThree
            "torrent-galaxy" -> TorrentGalaxy
            else -> throw IllegalArgumentException()
        }
    }
}