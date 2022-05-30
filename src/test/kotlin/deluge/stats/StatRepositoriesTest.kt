package deluge.stats

import Data.Companion.delugeTorrent
import dpozinen.App
import dpozinen.deluge.db.DataPointRepo
import dpozinen.deluge.db.DelugeTorrentRepo
import dpozinen.deluge.db.entities.DataPointEntity
import dpozinen.deluge.rest.DelugeTorrentConverter
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.iterable.ThrowingExtractor
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = [App::class])
@ActiveProfiles("test")
class StatRepositoriesTest {
    @Autowired
    lateinit var dataPointRepo: DataPointRepo

    @Autowired
    lateinit var delugeTorrentRepo: DelugeTorrentRepo

    @Autowired
    lateinit var converter: DelugeTorrentConverter

    @Test
    fun `should save data points`() {
        saveDataPoints()
        saveDataPoints()

        val dataPoints = dataPointRepo.findAll()
        assertThat(dataPoints).hasSize(2)
            .extracting(ThrowingExtractor { it.id })
            .doesNotContainNull()
    }

    @Test
    fun `should find data points`() {
        saveDataPoints("id-1")
        saveDataPoints("id-2")
        TimeUnit.SECONDS.sleep(1)
        val dp1 = saveDataPoints("id-1")
        val dp2 = saveDataPoints("id-2")

        val dataPoints = dataPointRepo.findTopOrderByTimeDescPerTorrent().associateBy { it.id }

        assertThat(dataPoints).hasSize(2)

        assertThat(dataPoints).extractingByKey(dp1.id).matches { it.isEqual(dp1) }

        assertThat(dataPoints).extractingByKey(dp2.id).matches { it.isEqual(dp2) }
    }

    private fun saveDataPoints(torrentId: String = delugeTorrent.id): DataPointEntity {
        delugeTorrentRepo.save(converter.convert(delugeTorrent))

        return dataPointRepo.save(
            DataPointEntity(
                torrentId = torrentId,
                uploaded = 1,
                downloaded = 1,
                upSpeed = 3,
                downSpeed = 4
            )
        )
    }

    @AfterEach
    private fun clear() {
        dataPointRepo.deleteAll()
        delugeTorrentRepo.deleteAll()
    }

}