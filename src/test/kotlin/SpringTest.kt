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
                jsonPath<String>("[0].name", `is`(Data.SEARCH_EXPECTED_TORRENT.name))
                jsonPath<String>("[0].name", `is`(Data.SEARCH_EXPECTED_TORRENT.name))
                jsonPath<String>("[0].size", `is`(Data.SEARCH_EXPECTED_TORRENT.size))
                jsonPath<Int>("[0].seeds", `is`(Data.SEARCH_EXPECTED_TORRENT.seeds))
                jsonPath<Int>("[0].leeches", `is`(Data.SEARCH_EXPECTED_TORRENT.leeches))
                jsonPath<String>("[0].date", `is`(Data.SEARCH_EXPECTED_TORRENT.date))
                jsonPath<String>("[0].contributor", `is`(Data.SEARCH_EXPECTED_TORRENT.contributor))
                jsonPath<Int>("[0].index", `is`(0))
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