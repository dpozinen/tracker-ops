package deluge.stats

import Data
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
        every { dataPointRepo.saveAll(listOf(dataPointA, dataPointB)) } returns listOf()

        statsService.updateStats()
    }

    @Test
    fun `should not update stats if same as last`() {
        val statsService = DelugeStatsService(dataPointRepo, delugeTorrentRepo, delugeService, converter)
        val torrents = allTorrents()

        every { delugeService.allTorrents() } returns torrents
        every { delugeTorrentRepo.saveAll(converter.convert(torrents)) } returns listOf()
        every { dataPointRepo.findTopOrderByTimeDescPerTorrent() } returns listOf(dataPointA)
        every { dataPointRepo.saveAll(listOf(dataPointB)) } returns listOf()

        statsService.updateStats()

        verify(exactly = 0) { dataPointRepo.saveAll(listOf(dataPointA, dataPointB)) }
    }

    @Test
    fun `should get stats`() {
        val statsService = DelugeStatsService(dataPointRepo, delugeTorrentRepo, delugeService, converter)
        val torrents = allTorrents().map { it.id }
        val from = LocalDateTime.now().minusHours(6)
        val to = LocalDateTime.now()

        every {
            dataPointRepo.findByTorrentsInTimeFrame(torrents, from, to)
        } returns listOf(dataPointA, dataPointA1, dataPointB)

        val stats = statsService.stats(torrents, from, to)

        assertThat(stats)
            .extractingByKey("123").isEqualTo(listOf(dataPointA, dataPointA1))

        assertThat(stats)
            .extractingByKey("456").isEqualTo(listOf(dataPointB))
    }

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
private val dataPointA = DataPointEntity(
    id = 1,
    torrentId = "123",
    downloaded = 1024 * 1024 * 1024,
    uploaded = 1024 * 1024 * 1024,
    upSpeed = 1024 * 1024 * 100,
    downSpeed = 1024 * 1024 * 10,
)
private val dataPointA1 = DataPointEntity(
    id = 2,
    torrentId = "123",
    downloaded = 1024 * 1024 * 1024,
    uploaded = 1024 * 1024 * 1024,
    upSpeed = 1024 * 1024 * 100,
    downSpeed = 1024 * 1024 * 10,
)

private val dataPointB = DataPointEntity(
    torrentId = "456",
    downloaded = 1024 * 1024 * 1024L * 2L,
    uploaded = 1024 * 1024 * 1024 * 2L,
    upSpeed = 1024 * 1024,
    downSpeed = 1024 * 1024 * 2,
)