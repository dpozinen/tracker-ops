package deluge.stats

import Data
import Data.Companion.dataPointA
import Data.Companion.dataPointA1
import Data.Companion.dataPointB
import Data.Companion.dataPointEntityA
import Data.Companion.dataPointEntityA1
import Data.Companion.dataPointEntityB
import dpozinen.deluge.core.DelugeService
import dpozinen.deluge.core.DelugeStatsService
import dpozinen.deluge.db.DataPointRepo
import dpozinen.deluge.db.DataPointRepo.Extensions.findByTorrentsInTimeFrame
import dpozinen.deluge.db.DelugeTorrentRepo
import dpozinen.deluge.db.entities.DataPointEntity
import dpozinen.deluge.kafka.StatsKafkaProducer
import dpozinen.deluge.rest.DelugeConverter
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDateTime
import kotlin.test.Test

//class StatServiceTest {
//
//    private val delugeService = mockk<DelugeService>()
//    private val converter = DelugeConverter()
//    private val kafkaProducer = mockk<StatsKafkaProducer>()
//
//    @Test
//    fun `should update stats`() {
//        val statsService = DelugeStatsService(delugeService, converter, kafkaProducer)
//        val torrents = allTorrents()
//
//        every { delugeService.allTorrents() } returns torrents
//
//        statsService.collectStats()
//    }
//
//    @Test
//    fun `should not update stats if same as last`() {
//        val statsService = DelugeStatsService(delugeService, converter, kafkaProducer)
//        val torrents = allTorrents()
//
//        every { delugeService.allTorrents() } returns torrents
//
//        statsService.collectStats()
//    }
//
//    private fun empty(entity: DataPointEntity) = DataPointEntity(
//        id = null,
//        torrentId = entity.torrentId,
//        downloaded = entity.downloaded,
//        uploaded = entity.uploaded,
//        upSpeed = entity.upSpeed,
//        downSpeed = entity.downSpeed,
//        time = null,
//    )
//
//    private fun allTorrents() = listOf(
//        Data.delugeTorrent.copy(
//            id = "123",
//            downloaded = "1 GiB",
//            uploaded = "1 GiB",
//            uploadSpeed = "100 MiB/s",
//            downloadSpeed = "10 MiB/s"
//        ),
//        Data.delugeTorrent.copy(
//            id = "456",
//            downloaded = "2 GiB",
//            uploaded = "2 GiB",
//            uploadSpeed = "1 MiB/s",
//            downloadSpeed = "2 MiB/s"
//        ),
//    )
//
//}
