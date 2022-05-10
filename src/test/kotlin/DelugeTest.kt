import dpozinen.App
import dpozinen.DelugeController
import org.assertj.core.api.Assertions.assertThat
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import kotlin.test.Test

// todo deluge controller test
class DelugeTest {

    private val runner = ApplicationContextRunner().withUserConfiguration(App::class.java)

    @Test
    fun `should disable deluge`() {
        runner.withPropertyValues(
            "tracker-ops.manual-deluge.enabled=false"
        ).run {
            assertThat(it).doesNotHaveBean(DelugeController::class.java)
        }

        runner.run {
            assertThat(it).doesNotHaveBean(DelugeController::class.java)
        }
    }

    @Test
    fun `should enable deluge`() {
        runner.withPropertyValues(
            "tracker-ops.manual-deluge.enabled=true",
            "tracker-ops.manual-deluge.download-folder=/",
            "tracker-ops.manual-deluge.address=192.1.2.3",
        ).run {
            assertThat(it).hasSingleBean(DelugeController::class.java)
        }
    }

}