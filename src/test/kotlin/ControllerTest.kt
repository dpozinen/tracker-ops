import Data.OneThreeThree.Companion.PAGE_EXPECTED_TORRENT
import Data.OneThreeThree.Companion.SEARCH_EXPECTED_TORRENT
import com.ninjasquad.springmockk.MockkBean
import dpozinen.App
import dpozinen.TrackerController
import dpozinen.TrackerService
import dpozinen.core.Trackers
import dpozinen.model.Torrents
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

    @Test @Disabled
    fun `should deluge`() {
        mockMvc.perform(
            MockMvcRequestBuilders.post("/deluge")
                .content("magnet:?xt=urn:btih:004658B2325068E5B75A76DAC63B9F78BA9EE9A2&dn=Robin+Carolan+-+The+Northman+%28Original+Motion+Picture+Soundtrack%29+%282022%29+%5B24Bit-48kHz%5D+FLAC+%5BPMEDIA%5D+%E2%AD%90%EF%B8%8F&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=udp%3A%2F%2Fopen.stealth.si%3A80%2Fannounce&tr=udp%3A%2F%2Fretracker.lanta-net.ru%3A2710%2Fannounce&tr=udp%3A%2F%2Ftracker.zerobytes.xyz%3A1337%2Fannounce&tr=udp%3A%2F%2Finferno.demonoid.is%3A3391%2Fannounce&tr=udp%3A%2F%2Fp4p.arenabg.com%3A1337%2Fannounce&tr=udp%3A%2F%2F9.rarbg.me%3A2980%2Fannounce&tr=udp%3A%2F%2Fexodus.desync.com%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.moeking.me%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.torrent.eu.org%3A451%2Fannounce&tr=udp%3A%2F%2Fopentor.org%3A2710%2Fannounce&tr=http%3A%2F%2Ftracker.files.fm%3A6969%2Fannounce&tr=udp%3A%2F%2Ffe.dealclub.de%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.openbittorrent.com%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=http%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=udp%3A%2F%2Fopentracker.i2p.rocks%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.internetwarriors.net%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969%2Fannounce&tr=udp%3A%2F%2Fcoppersurfer.tk%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.zer0day.to%3A1337%2Fannounce")
        ).andReturn()
    }
}