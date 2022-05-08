import com.ninjasquad.springmockk.MockkBean
import dpozinen.App
import dpozinen.TrackerController
import dpozinen.TrackerService
import dpozinen.core.Trackers
import dpozinen.model.Torrents
import io.mockk.every
import org.hamcrest.Matchers.`is`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.Test


@WebMvcTest(TrackerController::class)
@ContextConfiguration(classes = [App::class])
class SpringTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var service: TrackerService

    @Test
    fun `should search`() {
        every { service.search(Trackers.OneThreeThree, "abc abc") }
            .returns(Torrents(listOf(Data.SEARCH_EXPECTED_TORRENT)))

        mockMvc.get("/search/133/abc abc")
            .andExpect {
                jsonPath<String>("torrents[0].name", `is`(Data.SEARCH_EXPECTED_TORRENT.name))
                jsonPath<String>("torrents[0].link", `is`(Data.SEARCH_EXPECTED_TORRENT.link))
            }
    }

    @Test
    fun `should select`() {
        every { service.select(Trackers.OneThreeThree, 0) }.returns(Data.PAGE_EXPECTED_TORRENT)

        mockMvc.get("/search/133/abc abc/select/0")
            .andExpect {
                jsonPath<String>("name", `is`(Data.PAGE_EXPECTED_TORRENT.name))
                jsonPath<String>("link", `is`(Data.PAGE_EXPECTED_TORRENT.link))
            }
    }
}