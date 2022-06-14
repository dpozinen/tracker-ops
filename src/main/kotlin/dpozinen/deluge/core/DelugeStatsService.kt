@file:OptIn(ExperimentalTime::class)

package dpozinen.deluge.core

import dpozinen.deluge.db.DataPointRepo
import dpozinen.deluge.db.DataPointRepo.Extensions.findByTorrentsInTimeFrame
import dpozinen.deluge.db.DelugeTorrentRepo
import dpozinen.deluge.db.entities.DataPointEntity
import dpozinen.deluge.domain.DataPoint
import dpozinen.deluge.domain.DelugeTorrent
import dpozinen.deluge.mutations.By.Companion.bySize
import dpozinen.deluge.rest.DelugeConverter
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit.MINUTES
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime

@Service
@Profile("stats")
class DelugeStatsService(
    private val dataPointRepo: DataPointRepo,
    private val delugeTorrentRepo: DelugeTorrentRepo,
    private val delugeService: DelugeService,
    private val converter: DelugeConverter
) {

    fun updateStats() {
        val torrents = delugeService.allTorrents()

        delugeTorrentRepo.saveAll(converter.convert(torrents))

        dataPointRepo.saveAll(statsOf(torrents))
    }

    fun stats(
        torrentIds: Collection<String>,
        from: LocalDateTime, to: LocalDateTime,
        interval: Duration,
        minDataPoints: Int,
        fillEnd: Boolean
    ): Map<String, List<DataPoint>> {
        val ids = torrentIds.ifEmpty { delugeService.statefulTorrents().torrents.map { it.id } }
        return dataPointRepo.findByTorrentsInTimeFrame(ids, from, to)
            .map { converter.toDataPoint(it) }
            .groupBy { it.torrentId }
            .filter { it.value.size >= minDataPoints }
            .map { it.key to addMissingDataPoints(it.value, from, to, interval, fillEnd) }
            .toMap()
    }

    private fun addMissingDataPoints(
        points: List<DataPoint>,
        from: LocalDateTime,
        to: LocalDateTime,
        interval: Duration, // dptodo when interval is big there are issues with not finding closest value
        fillEnd: Boolean
    ): List<DataPoint> {
        val timeToPoint = points.associateBy { it.time }
        val first = points[0]
        var current = first

        var time = first.time
        while (time.isAfter(from)) time = time.minusMinutes(5)

        return asIntervals(time, to, interval)
            .map {
                current = timeToPoint[it] ?: current.emptyCopy(it)
                current
            }
            .toList()
            .let { dataPoints ->
                if (fillEnd) dataPoints
                else dataPoints.removeEndWhile { it.isEmptyCopy() }
            }
    }

    private fun asIntervals(time: LocalDateTime, to: LocalDateTime, interval: Duration)
            = generateSequence(time) {
        it.plusMinutes(interval.toLong(DurationUnit.MINUTES)).truncatedTo(MINUTES)
            .takeIf { timePlusInterval -> timePlusInterval.isBefore(to) }
    }

    private fun statsOf(torrents: Iterable<DelugeTorrent>): List<DataPointEntity> {
        val torrentToLast = dataPointRepo
            .findTopOrderByTimeDescPerTorrent()
            .associateBy { it.torrentId }

        return torrents
            .flatMap { toDataPoints(it) }
            .filterNot { isSameAsLast(it, torrentToLast) }
    }

    private fun isSameAsLast(dataPoint: DataPointEntity, torrentToLast: Map<String, DataPointEntity>) =
        torrentToLast[dataPoint.torrentId]?.isEqual(dataPoint) ?: false

    private fun toDataPoints(torrent: DelugeTorrent) = mutableListOf<DataPointEntity>().withDataPoints(torrent)

}

private fun <E> List<E>.removeEndWhile(predicate: (E) -> Boolean): List<E> {
    val list = this.toMutableList()
    val iterator = list.asReversed().iterator()

    for (e in iterator) if (predicate(e)) iterator.remove() else break

    return list
}

private fun MutableList<DataPointEntity>.withDataPoints(torrent: DelugeTorrent): MutableList<DataPointEntity> {
    fun toBytes(data: String) = bySize().comparable(data).toLong()

    this.add(
        DataPointEntity(
            id = null,
            torrentId = torrent.id,
            upSpeed = toBytes(torrent.uploadSpeed),
            downSpeed = toBytes(torrent.downloadSpeed),
            uploaded = toBytes(torrent.uploaded),
            downloaded = toBytes(torrent.downloaded),
        ))

    return this
}
