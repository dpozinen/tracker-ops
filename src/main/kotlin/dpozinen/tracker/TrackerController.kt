package dpozinen.tracker

import org.springframework.web.bind.annotation.*

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

    @PostMapping("/cookies/{tracker}")
    fun setCookies(@PathVariable tracker: String, @RequestBody cookies: String) {
        CookieStore.store(Trackers.from(tracker), cookies)
    }
}