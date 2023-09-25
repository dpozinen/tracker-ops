package deluge.stats

import com.ninjasquad.springmockk.MockkBean
import dpozinen.App
import dpozinen.deluge.core.DelugeStatsService
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.Durations.TEN_SECONDS
import org.awaitility.kotlin.atMost
import org.awaitility.kotlin.await
import org.awaitility.kotlin.untilAsserted
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
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
        every { delugeStatsService.collectStats() } answers { counter++ }

        await atMost TEN_SECONDS untilAsserted { assertThat(counter).isEqualTo(3) }
    }

}