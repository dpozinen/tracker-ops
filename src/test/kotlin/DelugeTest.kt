import com.ninjasquad.springmockk.MockkBean
import dpozinen.App
import dpozinen.deluge.DelugeClientException
import dpozinen.deluge.DelugeController
import dpozinen.deluge.DelugeResponse
import dpozinen.deluge.DelugeService
import io.mockk.every
import org.assertj.core.api.Assertions.assertThat
import org.hamcrest.Matchers
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Disabled
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.runner.ApplicationContextRunner
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.client.match.MockRestRequestMatchers.jsonPath
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
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

    @WebMvcTest(DelugeController::class)
    @ContextConfiguration(classes = [App::class])
    class ControllerTest(@Autowired val mockMvc: MockMvc) {

        @MockkBean
        private lateinit var service: DelugeService

        @Test
        @Disabled // todo exception garbage
        fun `should handle deluge response`() {
            every { service.login() } returns "session"

            try {
                every {
                    service.addMagnet("session", Data.magnet)
                } throws DelugeClientException(DelugeResponse(false, 1, mapOf()))
            } catch (ignored: Throwable) {}

            mockMvc.perform(post("/deluge").content(Data.magnet))
                .andExpect {
                    assertThat(it.response.status).isEqualTo(400)
                    jsonPath("result", `is`("null"))
                    jsonPath("id", `is`(1))
                    jsonPath<Map<*, *>>("error", `is`(Matchers.anEmptyMap()))
                }
        }

    }

}