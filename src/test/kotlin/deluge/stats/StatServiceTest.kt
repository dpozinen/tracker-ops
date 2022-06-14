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
import dpozinen.deluge.rest.DelugeConverter
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

class StatServiceTest {

    private val dataPointRepo: DataPointRepo = mockk()
    private val delugeTorrentRepo: DelugeTorrentRepo = mockk()
    private val delugeService = mockk<DelugeService>()
    private val converter = DelugeConverter()

    @Test
    fun `should update stats`() {
        val statsService = DelugeStatsService(dataPointRepo, delugeTorrentRepo, delugeService, converter)
        val torrents = allTorrents()

        every { delugeService.allTorrents() } returns torrents
        every { delugeTorrentRepo.saveAll(converter.convert(torrents)) } returns listOf()
        every { dataPointRepo.findTopOrderByTimeDescPerTorrent() } returns listOf()
        every { dataPointRepo.saveAll(listOf(empty(dataPointEntityA), empty(dataPointEntityB))) } returns listOf()

        statsService.updateStats()
    }

    @Test
    fun `should not update stats if same as last`() {
        val statsService = DelugeStatsService(dataPointRepo, delugeTorrentRepo, delugeService, converter)
        val torrents = allTorrents()

        every { delugeService.allTorrents() } returns torrents
        every { delugeTorrentRepo.saveAll(converter.convert(torrents)) } returns listOf()
        every { dataPointRepo.findTopOrderByTimeDescPerTorrent() } returns listOf(dataPointEntityA)
        every { dataPointRepo.saveAll(listOf(empty(dataPointEntityB))) } returns listOf()

        statsService.updateStats()

        verify(exactly = 0) { dataPointRepo.saveAll(listOf(empty(dataPointEntityA), empty(dataPointEntityB))) }
    }

    @OptIn(ExperimentalTime::class)
    @Test
    fun `should get stats`() {
        val statsService = DelugeStatsService(dataPointRepo, delugeTorrentRepo, delugeService, converter)
        val torrents = allTorrents().map { it.id }
        val from = Data.now.minusHours(6)
        val to = Data.now.plusHours(5)

        every {
            dataPointRepo.findByTorrentsInTimeFrame(torrents, from, to)
        } returns listOf(dataPointEntityA, dataPointEntityA1, dataPointEntityB)

        val stats = statsService.stats(torrents, from, to, Duration.parse("5m"), 0, false)

        assertThat(stats)
            .extractingByKey("123").asList().contains(dataPointA, dataPointA1)

        assertThat(stats)
            .extractingByKey("456").asList().contains(dataPointB)
    }

    private fun empty(entity: DataPointEntity) = DataPointEntity(
        id = null,
        torrentId = entity.torrentId,
        downloaded = entity.downloaded,
        uploaded = entity.uploaded,
        upSpeed = entity.upSpeed,
        downSpeed = entity.downSpeed,
        time = null,
    )

    private fun allTorrents() = listOf(
        Data.delugeTorrent.copy(
            id = "123",
            downloaded = "1 GiB",
            uploaded = "1 GiB",
            uploadSpeed = "100 MiB/s",
            downloadSpeed = "10 MiB/s"
        ),
        Data.delugeTorrent.copy(
            id = "456",
            downloaded = "2 GiB",
            uploaded = "2 GiB",
            uploadSpeed = "1 MiB/s",
            downloadSpeed = "2 MiB/s"
        ),
    )

}
