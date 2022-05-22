package tracker

import Data
import Data.OneThreeThree.Companion.PAGE_EXPECTED_TORRENT
import Data.OneThreeThree.Companion.SEARCH_EXPECTED_TORRENT
import com.ninjasquad.springmockk.MockkBean
import dpozinen.App
import dpozinen.tracker.TrackerController
import dpozinen.tracker.TrackerService
import dpozinen.tracker.Trackers
import dpozinen.tracker.Torrents
import io.mockk.every
import org.hamcrest.Matchers.`is`
import org.junit.jupiter.api.Disabled
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders
import kotlin.test.Test


@WebMvcTest(TrackerController::class)
@ContextConfiguration(classes = [App::class])
class ControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    private lateinit var service: TrackerService

    @Test
    fun `should search`() {
        every { service.search(Trackers.OneThreeThree, "abc abc") }
            .returns(Torrents(listOf(SEARCH_EXPECTED_TORRENT)))

        mockMvc.get("/search/133/abc abc")
            .andExpect {
                jsonPath<String>("[0].name", `is`(SEARCH_EXPECTED_TORRENT.name))
                jsonPath<String>("[0].name", `is`(SEARCH_EXPECTED_TORRENT.name))
                jsonPath<String>("[0].size", `is`(SEARCH_EXPECTED_TORRENT.size))
                jsonPath<Int>("[0].seeds", `is`(SEARCH_EXPECTED_TORRENT.seeds))
                jsonPath<Int>("[0].leeches", `is`(SEARCH_EXPECTED_TORRENT.leeches))
                jsonPath<String>("[0].date", `is`(SEARCH_EXPECTED_TORRENT.date))
                jsonPath<String>("[0].contributor", `is`(SEARCH_EXPECTED_TORRENT.contributor))
                jsonPath<Int>("[0].index", `is`(0))
                jsonPath<String>("[0].link", `is`("http://192.168.0.130:8133/search/133/abc%20abc/select/0"))
            }
    }

    @Test
    fun `should select`() {
        every { service.select(Trackers.OneThreeThree, "abc abc", 0) }
            .returns(PAGE_EXPECTED_TORRENT)

        mockMvc.get("/search/133/abc abc/select/0")
            .andExpect {
                jsonPath<String>("name", `is`(PAGE_EXPECTED_TORRENT.name))
                jsonPath<String>("link", `is`(PAGE_EXPECTED_TORRENT.link))
            }
    }

}