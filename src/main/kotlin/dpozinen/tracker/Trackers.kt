package dpozinen.tracker

enum class Trackers {
    OneThreeThree, TorrentGalaxy, DigitalCore;

    companion object {
        fun from(name: String) = when (name) {
            "133" -> OneThreeThree
            "torrent-galaxy" -> TorrentGalaxy
            "digital-core" -> DigitalCore
            else -> throw IllegalArgumentException()
        }
    }
}