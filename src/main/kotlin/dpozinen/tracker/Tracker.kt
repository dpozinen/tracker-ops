package dpozinen.tracker

class Tracker(
    private val parser: TrackerParser,
    private val ops: TrackerOps,
    private val torrents: MutableMap<String, Torrents> = mutableMapOf()
) {

    fun search(keywords: String): Torrents {
        return torrents.computeIfAbsent(keywords) {
            val body = ops.search(keywords.split(" "))
            parser.parseSearch(body)
        }
    }

    fun select(keywords: String, index: Int): Torrent {
        val searchResult = this.torrents[keywords] ?: throw IllegalArgumentException(keywords)
        val torrent = searchResult.torrents[index]
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