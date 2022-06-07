package deluge.stats

import com.ninjasquad.springmockk.MockkBean
import dpozinen.App
import dpozinen.deluge.core.DelugeStatsService
import io.mockk.every
import org.assertj.core.api.Assertions
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.TimeUnit
import kotlin.test.Test

@SpringBootTest(
    classes = [App::class],
    properties = ["tracker-ops.deluge.stats.poll-interval=3s"]
)
@ActiveProfiles("test", "stats", "job-test")
class StatJobTest {

    @MockkBean
    lateinit var delugeStatsService: DelugeStatsService

    @Test
    fun `should run job`() {
        var counter = 0
        every { delugeStatsService.updateStats() } answers { counter++ }

        TimeUnit.SECONDS.sleep(10)
        Assertions.assertThat(counter).isEqualTo(3)
    }

}