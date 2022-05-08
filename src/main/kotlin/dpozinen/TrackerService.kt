package dpozinen

import dpozinen.core.Tracker
import dpozinen.core.Trackers
import dpozinen.model.Torrent
import dpozinen.model.Torrents
import org.springframework.stereotype.Service

@Service
open class TrackerService(private val cache: MutableMap<Trackers, Tracker> = mutableMapOf()) {

//  todo cache search results per keywords

    fun search(trackers: Trackers, keywords: String): Torrents {
        val tracker = cache.computeIfAbsent(trackers) { Tracker.from(trackers) }

        return tracker.search(keywords.split(" "))
    }

    fun select(trackers: Trackers, index: Int): Torrent {
        val tracker = cache.getOrElse(trackers) { throw IllegalArgumentException(trackers.name) }

        return tracker.select(index)
    }

}