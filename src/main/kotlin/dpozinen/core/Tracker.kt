package dpozinen.core

import dpozinen.model.Torrent
import dpozinen.model.Torrents

class Tracker(
    private val parser: TrackerParser,
    private val ops: TrackerOps,
    private var torrents: Torrents = Torrents.empty()
) {

    fun search(keywords: List<String>): Torrents {
        val body = ops.search(keywords)

        this.torrents = parser.parseSearch(body)

        return torrents
    }

    fun select(index: Int): Torrent {
        val torrent = torrents.torrents[index]
        val torrentPage = ops.open(torrent)
        return parser.parseTorrentPage(torrentPage).replaceMissing(torrent)
    }

    companion object {

        fun from(tracker: Trackers) =
            when (tracker) {
                Trackers.OneThreeThree -> oneThreeThreeSevenXTo()
            }

        private fun oneThreeThreeSevenXTo() = Tracker(
            TrackerParser.OneThreeThree(),
            TrackerOps.OneThreeThree("https://1337x.to")
        )

    }

}