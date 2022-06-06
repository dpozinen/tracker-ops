package dpozinen.deluge.rest


import dpozinen.deluge.core.DelugeService
import dpozinen.deluge.core.DelugeStatsService
import dpozinen.deluge.domain.Stats
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.ZoneId
import java.time.ZoneOffset
import java.util.*

@RestController
@Profile("stats")
class DelugeStatsController(
    private val statsService: DelugeStatsService,
    private val torrentService: DelugeService,
    private val converter: DelugeConverter,
) {

    @GetMapping("/deluge/stats")
    fun stats(
        @RequestParam("torrentIds", defaultValue = "") torrentIds: Set<String>,
        @RequestParam("ago", required = false) timeAgo: String?,
        @RequestParam("from") timeFrom: String?,
        @RequestParam("to") timeTo: String?
    ): Stats {
        val stats =  Optional.ofNullable(timeAgo)
            .map { statsService.stats(torrentIds, converter.toLocalDateTime(it), now()) }
            .orElseGet {
                statsService.stats(
                    torrentIds,
                    LocalDateTime.parse(timeFrom) ?: now().minusHours(6),
                    LocalDateTime.parse(timeTo) ?: now())
            }

        val torrents = torrentService.allTorrents()

        return Stats(torrents, stats)
    }

}