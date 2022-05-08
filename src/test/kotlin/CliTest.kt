import Data.Companion.PAGE_EXPECTED_TORRENT
import Data.Companion.SEARCH_EXPECTED_TORRENT
import Data.Companion.SEARCH_PAGE_PATH
import Data.Companion.TORRENT_PAGE_PATH
import dpozinen.core.Tracker
import dpozinen.core.TrackerOps
import dpozinen.core.TrackerParser
import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test

class CliTest {

    @Test
    fun `should get torrents`() {
        val body = Files.readString(Path.of(SEARCH_PAGE_PATH))
        val torrents = TrackerParser.OneThreeThree().parseSearch(body)

        assertThat(torrents.torrents).containsExactly(SEARCH_EXPECTED_TORRENT)
    }

    @Test
    fun `should get torrent link`() {
        val body = Files.readString(Path.of(TORRENT_PAGE_PATH))
        val torrent = TrackerParser.OneThreeThree().parseTorrentPage(body)

        assertThat(torrent).isEqualTo(PAGE_EXPECTED_TORRENT)
    }

    @Test
    fun `should work`() {
        val (ops, keywords, torrentPage) = mock()

        val tracker = Tracker(TrackerParser.OneThreeThree(), ops)
        val search = tracker.search(keywords)

        Mockito.`when`(ops.open(search.torrents[0])).thenReturn(torrentPage)

        val magnet = tracker.select(0)

        assertThat(magnet).isEqualTo(PAGE_EXPECTED_TORRENT)
    }

    private fun mock(): Triple<TrackerOps, MutableList<String>, String> {
        val ops = Mockito.mock(TrackerOps::class.java)
        val keywords = mutableListOf("a", "b")
        val searchPage = Files.readString(Path.of(SEARCH_PAGE_PATH))
        val torrentPage = Files.readString(Path.of(TORRENT_PAGE_PATH))

        Mockito.`when`(ops.search(keywords)).thenReturn(searchPage)

        return Triple(ops, keywords, torrentPage)
    }

}