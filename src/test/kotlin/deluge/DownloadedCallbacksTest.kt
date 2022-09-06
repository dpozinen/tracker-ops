package deluge

import dpozinen.App
import dpozinen.deluge.core.DownloadedCallbacks
import kotlinx.coroutines.runBlocking
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatNoException
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest(
    properties = [
        "tracker-ops.follow-duration=3m",
        "logging.level.dpozinen.deluge.core.DownloadedCallbacks=debug"],
    classes = [App::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
class DownloadedCallbacksTest {

    @Autowired
    lateinit var downloadedCallbacks: DownloadedCallbacks

    @Test @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Only local")
    fun `trigger completed move`() {
        assertThatNoException().isThrownBy {
            downloadedCallbacks.trueNasMove()
            downloadedCallbacks.plexScanLib()
        }
    }

    @Test @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Only local")
    fun `follow torrent download`() {
        val ok = AtomicInteger(0)
        runBlocking {
            downloadedCallbacks.follow(Data.delugeTorrent) {
                if (ok.get() == 2)
                    listOf(Data.delugeTorrent.copy(state = "Seeding"))
                else {
                    ok.incrementAndGet()
                    listOf()
                }
            }
            assertThat(ok).hasValue(2)
        }
    }

    @Test @DisabledIfEnvironmentVariable(named = "CI", matches = "true", disabledReason = "Only local")
    fun `follow torrent download for too long`() {
        val ok = AtomicInteger(0)
        runBlocking {
            downloadedCallbacks.follow(Data.delugeTorrent) {
                ok.incrementAndGet()
                listOf()
            }
            assertThat(ok).hasValue(3)
        }
    }
}