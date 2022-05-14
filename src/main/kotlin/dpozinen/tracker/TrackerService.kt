package dpozinen.tracker

import org.springframework.stereotype.Service

@Service
open class TrackerService(private val trackers: MutableMap<Trackers, Tracker> = mutableMapOf()) {

    fun search(name: Trackers, keywords: String): Torrents {
        val tracker = trackers.computeIfAbsent(name) { Tracker.from(name) }

        return tracker.search(keywords)
    }

    fun select(name: Trackers, keywords: String, index: Int): Torrent {
        val tracker = trackers[name] ?: throw IllegalArgumentException(name.name)

        return tracker.select(keywords, index)
    }

}