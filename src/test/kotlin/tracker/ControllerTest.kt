package tracker

import Data.OneThreeThree.Companion.PAGE_EXPECTED_TORRENT
import Data.OneThreeThree.Companion.SEARCH_EXPECTED_TORRENT
import com.ninjasquad.springmockk.MockkBean
import dpozinen.App
import dpozinen.tracker.Torrents
import dpozinen.tracker.TrackerController
import dpozinen.tracker.TrackerService
import dpozinen.tracker.Trackers
import io.mockk.every
import org.hamcrest.Matchers.`is`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import kotlin.test.Test


@WebMvcTest(TrackerController::class)
@ContextConfiguration(classes = [App::class])
@ActiveProfiles("test")
class ControllerTest(@Autowired val mockMvc: MockMvc,
                     @Value("\${tracker-ops.host:localhost}") private val host: String,
                     @Value("\${server.port:8133}") private val port: String,) {

    @MockkBean
    private lateinit var service: TrackerService

    @Test
    fun `should sort results qxr first`() {
        every { service.search(Trackers.OneThreeThree, "abc abc") }
            .returns(Torrents(listOf(
                SEARCH_EXPECTED_TORRENT.copy(contributor = "abc"),
                SEARCH_EXPECTED_TORRENT,
                SEARCH_EXPECTED_TORRENT.copy(contributor = ""),
                SEARCH_EXPECTED_TORRENT.copy(contributor = "qxr"),
            )))

        mockMvc.get("/search/133/abc abc")
            .andExpect {
                jsonPath<String>("[0].contributor", `is`("qxr"))
                jsonPath<String>("[1].contributor", `is`("QxR"))
                jsonPath<String>("[2].contributor", `is`("abc"))
                jsonPath<String>("[3].contributor", `is`(""))
            }
    }

    @Test
    fun `should search`() {
        every { service.search(Trackers.OneThreeThree, "abc abc") }
            .returns(Torrents(listOf(SEARCH_EXPECTED_TORRENT)))

        mockMvc.get("/search/133/abc abc")
            .andExpect {
                jsonPath<String>("[0].name", `is`(SEARCH_EXPECTED_TORRENT.name))
                jsonPath<String>("[0].size", `is`(SEARCH_EXPECTED_TORRENT.size))
                jsonPath<Int>("[0].seeds", `is`(SEARCH_EXPECTED_TORRENT.seeds))
                jsonPath<Int>("[0].leeches", `is`(SEARCH_EXPECTED_TORRENT.leeches))
                jsonPath<String>("[0].date", `is`(SEARCH_EXPECTED_TORRENT.date))
                jsonPath<String>("[0].contributor", `is`(SEARCH_EXPECTED_TORRENT.contributor))
                jsonPath<Int>("[0].index", `is`(0))
                jsonPath<String>("[0].link", `is`("http://$host:$port/search/133/abc%20abc/select/0"))
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