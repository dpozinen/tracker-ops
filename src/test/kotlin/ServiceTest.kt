import dpozinen.TrackerService
import dpozinen.core.Tracker
import dpozinen.core.TrackerOps
import dpozinen.core.TrackerParser
import dpozinen.core.Trackers.*
import dpozinen.model.Torrents
import io.mockk.every
import io.mockk.mockkClass
import io.mockk.verify
import org.assertj.core.api.Assertions
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test


class ServiceTest {

    @Test
    fun `should use results cache`() {
        val (keywords, parser, ops) = mock()
        val tracker = Tracker(parser, ops)

        tracker.search(keywords)
        tracker.search(keywords)
        tracker.search(keywords)
        tracker.search(keywords)

        verify(exactly = 1) { ops.search(keywords.split(" ")) }
    }

    @Test
    fun `should use tracker cache`() {
        val (keywords, parser, ops) = mock()
        val tracker = Tracker(parser, ops)

        val trackers = mutableMapOf(Pair(OneThreeThree, tracker))
        val service = TrackerService(trackers)

        service.search(OneThreeThree, keywords)

        assertThat(trackers).hasSize(1)
        assertThat(trackers[OneThreeThree]).isEqualTo(tracker)
    }

    private fun mock(): Triple<String, TrackerParser, TrackerOps> {
        val keywords = "a b c"
        val parser = mockkClass(TrackerParser::class)
        val ops = mockkClass(TrackerOps::class)

        every { ops.search(keywords.split(" ")) } returns ("")
        every { parser.parseSearch("") } returns (Torrents.empty())

        return Triple(keywords, parser, ops)
    }

}