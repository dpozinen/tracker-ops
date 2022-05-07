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
        val torrentPage = ops.open(torrents.torrents[index])
        return parser.parseTorrentPage(torrentPage)
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