package tracker

import Data.OneThreeThree.Companion.PAGE_COMPLETE_EXPECTED_TORRENT
import Data.OneThreeThree.Companion.SEARCH_PAGE_PATH
import Data.OneThreeThree.Companion.TORRENT_PAGE_PATH
import dpozinen.tracker.Tracker
import dpozinen.tracker.TrackerOps
import dpozinen.tracker.TrackerParser
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test

class CliTest { // todo not really cli test

    @Test
    fun `should work`() {
        val (ops, keywords, torrentPage) = mock()

        val tracker = Tracker(TrackerParser.OneThreeThree(), ops)
        val search = tracker.search(keywords)

        Mockito.`when`(ops.open(search.torrents[0])).thenReturn(torrentPage)

        val magnet = tracker.select(keywords, 0)

        assertThat(magnet).isEqualTo(PAGE_COMPLETE_EXPECTED_TORRENT)
    }

    private fun mock(): Triple<TrackerOps, String, String> {
        val ops = Mockito.mock(TrackerOps::class.java)
        val keywords = "a b"
        val searchPage = Files.readString(Path.of(SEARCH_PAGE_PATH))
        val torrentPage = Files.readString(Path.of(TORRENT_PAGE_PATH))

        Mockito.`when`(ops.search(keywords.split(" "))).thenReturn(searchPage)

        return Triple(ops, keywords, torrentPage)
    }

}