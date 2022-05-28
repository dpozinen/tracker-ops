package dpozinen.deluge

import dpozinen.deluge.db.DataPointRepo
import dpozinen.deluge.db.DelugeTorrentRepo
import dpozinen.deluge.db.entities.DataPointEntity
import dpozinen.deluge.db.entities.DataPointEntity.Graph.*
import dpozinen.deluge.mutations.By.Companion.bySize
import dpozinen.deluge.rest.DelugeTorrentConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Service
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime

@Service
class DelugeStatsService(
    private val dataPointRepo: DataPointRepo,
    private val delugeTorrentRepo: DelugeTorrentRepo,
    private val delugeService: DelugeService,
    private val converter: DelugeTorrentConverter
) {

    @Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
    @OptIn(ExperimentalTime::class)
    @EventListener(ApplicationReadyEvent::class)
    fun startJob() {
        runBlocking {
            launch(Dispatchers.IO) {
                repeat(Int.MAX_VALUE) {
                    val torrents = delugeService.allTorrents()

                    delugeTorrentRepo.saveAll(converter.convert(torrents))

                    dataPointRepo.saveAll(statsOf(torrents))

                    delay(minutes(5))
                }
            }
        }
    }

    private fun statsOf(torrents: Iterable<DelugeTorrent>) = torrents.flatMap { toDataPoints(it) }

    private fun toDataPoints(torrent: DelugeTorrent) = mutableListOf<DataPointEntity>().withDataPoints(torrent)

}

private fun MutableList<DataPointEntity>.withDataPoints(torrent: DelugeTorrent): MutableList<DataPointEntity> {
    fun toBytes(data: String): Long? {
        val bytes = bySize().comparable(data)
        return if (bytes == 0.0) null else bytes.toLong()
    }

    DataPointEntity.Graph.values().forEach { graph ->
        when (graph) {
            UP_SPEED -> toBytes(torrent.uploadSpeed)
            DOWN_SPEED -> toBytes(torrent.downloadSpeed)
            UPLOADED -> toBytes(torrent.uploaded)
            DOWNLOADED -> toBytes(torrent.downloaded)
        }?.also {
            if (it > 0) this.add(DataPointEntity(torrentId = torrent.id, graph = graph, data = it))
        }
    }

    return this
}
