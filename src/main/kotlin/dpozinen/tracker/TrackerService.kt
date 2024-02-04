package dpozinen.tracker

open class TrackerService(private val trackers: Map<Trackers, Tracker>) {

    fun search(name: Trackers, keywords: String): Torrents {
        val tracker = trackers[name] ?: throw IllegalArgumentException(name.name)

        return tracker.search(keywords)
    }

    fun select(name: Trackers, keywords: String, index: Int): Torrent {
        val tracker = trackers[name] ?: throw IllegalArgumentException(name.name)

        return tracker.select(keywords, index)
    }

}