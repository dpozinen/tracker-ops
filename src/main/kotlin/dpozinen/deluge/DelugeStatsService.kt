package dpozinen.deluge

import dpozinen.deluge.db.DataPointRepo
import dpozinen.deluge.db.DelugeTorrentRepo
import dpozinen.deluge.db.entities.DataPointEntity
import dpozinen.deluge.mutations.By.Companion.bySize
import dpozinen.deluge.rest.DelugeTorrentConverter
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service

@Service
@Profile("stats")
class DelugeStatsService(
    private val dataPointRepo: DataPointRepo,
    private val delugeTorrentRepo: DelugeTorrentRepo,
    private val delugeService: DelugeService,
    private val converter: DelugeTorrentConverter
) {

    fun updateStats() {
        val torrents = delugeService.allTorrents()

        delugeTorrentRepo.saveAll(converter.convert(torrents))

        dataPointRepo.saveAll(statsOf(torrents))
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

private fun MutableList<DataPointEntity>.withDataPoints(torrent: DelugeTorrent): MutableList<DataPointEntity> {
    fun toBytes(data: String) = bySize().comparable(data).toLong()

    this.add(DataPointEntity(
        id = null,
        torrentId = torrent.id,
        upSpeed = toBytes(torrent.uploadSpeed),
        downSpeed = toBytes(torrent.downloadSpeed),
        uploaded = toBytes(torrent.uploaded),
        downloaded = toBytes(torrent.downloaded),
    ))

    return this
}
