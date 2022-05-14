package dpozinen.tracker

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RestController

@RestController
open class TrackerController(private val service: TrackerService) {


    @GetMapping("/search/{tracker}/{keywords}")
    fun search(@PathVariable tracker: String, @PathVariable keywords: String): Torrents {
        return service.search(Trackers.from(tracker), keywords)
    }

    @GetMapping("/search/{tracker}/{keywords}/select/{index}")
    fun select(@PathVariable tracker: String, @PathVariable keywords: String, @PathVariable index: Int): Torrent {
        return service.select(Trackers.from(tracker), keywords, index)
    }
}