package dpozinen.deluge.rest


import dpozinen.deluge.core.DelugeService
import dpozinen.deluge.core.DelugeStatsService
import dpozinen.deluge.db.entities.DataPointEntity
import dpozinen.deluge.domain.DataPoint
import dpozinen.deluge.domain.Stats
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import javax.websocket.server.PathParam
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@RestController("/deluge/stats")
class DelugeStatsController(
    private val statsService: DelugeStatsService,
    private val torrentService: DelugeService,
    private val converter: DelugeConverter,
) {

    @GetMapping
    fun stats(
        @PathParam("torrentIds") torrentIds: Collection<String>,
        @PathParam("ago") timeAgo: String?,
        @PathParam("from") timeFrom: LocalDateTime = LocalDateTime.now().minusHours(6),
        @PathParam("to") timeTo: LocalDateTime = LocalDateTime.now()
    ): Stats {
        val stats =  Optional.ofNullable(timeAgo)
            .map { statsService.stats(torrentIds, converter.toLocalDateTime(it), timeTo) }
            .orElseGet { statsService.stats(torrentIds, timeFrom, timeTo) }

        val torrents = torrentService.allTorrents()

        return Stats(torrents, stats)
    }

}