package dpozinen.deluge.rest


import dpozinen.deluge.core.DelugeService
import dpozinen.deluge.core.DelugeStatsService
import dpozinen.deluge.domain.Stats
import org.springframework.context.annotation.Profile
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime
import java.time.LocalDateTime.now
import java.time.LocalDateTime.parse
import java.time.temporal.ChronoUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@RestController
@Profile("stats")
class DelugeStatsController(
    private val statsService: DelugeStatsService,
    private val torrentService: DelugeService,
    private val converter: DelugeConverter,
) {

    @OptIn(ExperimentalTime::class)
    @GetMapping("/deluge/stats")
    fun stats(
        @RequestParam("torrentIds", defaultValue = "") torrentIds: Set<String>,
        @RequestParam("ago", required = false) timeAgo: String?,
        @RequestParam("from", required = false) from: String?,
        @RequestParam("to", required = false) to: String?,
        @RequestParam("minPoints", defaultValue = "3") minPoints: Int,
        @RequestParam("interval",  defaultValue = "5m") interval: String,
        @RequestParam("fillEnd",  defaultValue = "false") fillEnd: Boolean,
    ): Stats {
        val timeFrom = timeAgo
            ?.let { converter.toLocalDateTime(it) }
            ?: from?.takeUnless { it.isBlank() }?.let { parse(it) }
            ?: now().minusHours(6)
        val timeTo = to?.takeUnless { it.isBlank() }?.let { parse(it) } ?: now()
        val stats = statsService.stats(torrentIds, timeFrom, timeTo, Duration.parse(interval), minPoints, fillEnd)
            .filterNot { it.value.isEmpty() }

        val intervals = generateSequence(timeFrom) { it.plusMinutes(5).takeIf { it.isBefore(timeTo) } }
            .map { it.toLocalTime().truncatedTo(ChronoUnit.MINUTES).toString() }
            .toMutableList()

        val torrents = torrentService.allTorrents()
            .map { it.copy(name = it.name.substringBefore("(").replace(".", " ").take(30)) }

        return Stats(torrents, stats, intervals)
    }

}