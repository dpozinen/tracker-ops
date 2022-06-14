package deluge.stats

import Data
import com.ninjasquad.springmockk.MockkBean
import dpozinen.App
import dpozinen.deluge.core.DelugeService
import dpozinen.deluge.core.DelugeStatsService
import dpozinen.deluge.rest.DelugeConverter
import dpozinen.deluge.rest.DelugeStatsController
import io.mockk.every
import org.hamcrest.Matcher
import org.hamcrest.Matchers.`is`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MockMvcResultMatchersDsl
import org.springframework.test.web.servlet.get
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@WebMvcTest(DelugeStatsController::class)
@ContextConfiguration(classes = [App::class, StatsControllerTest.Config::class])
@ActiveProfiles("test", "stats")
class StatsControllerTest(@Autowired val mockMvc: MockMvc) {

    @MockkBean
    lateinit var statsService: DelugeStatsService

    @MockkBean
    lateinit var torrentService: DelugeService

    @OptIn(ExperimentalTime::class)
    @Test
    fun `should get stats`() {
        every {
            statsService.stats(setOf("123"), Data.now.minusHours(5), Data.now, Duration.parse("5m"), 3, false)
        } returns mapOf("123" to listOf(Data.dataPointA1, Data.dataPointA))

        every {
            torrentService.allTorrents()
        } returns listOf(Data.delugeTorrent.copy(id = "123"))

        mockMvc.get("/deluge/stats") {
            param("from", Data.now.minusHours(5).toString())
            param("to", Data.now.toString())
            param("torrentIds", "123")
        }.andExpect {
                eq("stats['123'][0].id" to `is`(2))
                eq("stats['123'][1].id" to `is`(1))
                eq("torrents[0].id" to `is`("123"))
            }
    }


    @Test
    fun `should get stats default time`() {
        every {
            statsService.stats(any(), any(), any(),any(), any(), any())
        } returns mapOf("123" to listOf(Data.dataPointA1, Data.dataPointA))

        every {
            torrentService.allTorrents()
        } returns listOf(Data.delugeTorrent.copy(id = "123"))

        mockMvc.get("/deluge/stats")
            .andExpect {
                eq("stats['123'][0].id" to `is`(2))
                eq("stats['123'][1].id" to `is`(1))
                eq("torrents[0].id" to `is`("123"))
            }
    }

    @Test
    fun `should get stats relative`() {
        every {
            statsService.stats(eq(setOf("123")), any(), any(), any(), any(), any())
        } returns mapOf("123" to listOf(Data.dataPointA1, Data.dataPointA))

        every {
            torrentService.allTorrents()
        } returns listOf(Data.delugeTorrent.copy(id = "123"))

        mockMvc.get("/deluge/stats") {
            param("ago", "6m")
            param("torrentIds", "123")
        }.andExpect {
            eq("stats['123'][0].id" to `is`(2))
            eq("stats['123'][1].id" to `is`(1))
            eq("torrents[0].id" to `is`("123"))
        }
    }

    @TestConfiguration
    open class Config {
        @Bean
        open fun converter(): DelugeConverter = DelugeConverter()
    }
}

fun <T> MockMvcResultMatchersDsl.eq(pair: Pair<String, Matcher<T>>) {
    return jsonPath(pair.first, pair.second)
}
