package deluge

import Data
import com.ninjasquad.springmockk.MockkBean
import dpozinen.App
import dpozinen.deluge.core.DelugeDownloadFollower
import dpozinen.deluge.core.DownloadedCallbacks
import io.mockk.coVerify
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest(
    properties = [
        "tracker-ops.follow-duration=PT6S",
        "tracker-ops.follow-interval=PT2S",
        "logging.level.dpozinen.deluge.core.DownloadedCallbacks=debug"],
    classes = [App::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
class DownloadedFollowTest {

    @MockkBean
    lateinit var downloadedCallbacks: DownloadedCallbacks

    @Autowired
    lateinit var follower: DelugeDownloadFollower

    @Test
//    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Only local")
    fun `follow torrent download`() {
        val ok = AtomicInteger(0)
        val victim = Data.delugeTorrentResponse.copy(state = "Seeding", downloaded = 1.0)
        runBlocking {
            follower.follow(Data.delugeTorrentResponse) {
                if (ok.get() == 2) {
                    listOf(victim)
                } else {
                    ok.incrementAndGet()
                    listOf()
                }
            }
        }
        assertThat(ok).hasValue(2)
        coVerify(exactly = 1) { downloadedCallbacks.trigger(victim, any()) }
    }

    @Test
//    @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Only local")
    fun `follow torrent download for too long`() {
        val ok = AtomicInteger(0)
        runBlocking {
            follower.follow(Data.delugeTorrentResponse) {
                ok.incrementAndGet()
                listOf()
            }
            assertThat(ok).hasValue(3)
        }
    }
}