import Data.OneThreeThree.PAGE_EXPECTED_TORRENT
import Data.OneThreeThree.SEARCH_EXPECTED_TORRENT
import Data.OneThreeThree.SEARCH_PAGE_PATH
import Data.OneThreeThree.TORRENT_PAGE_PATH
import dpozinen.core.TrackerParser
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import java.nio.file.Files
import java.nio.file.Path

class ParsingTest {

    @Test
    fun `should parse search page 1337x`() {
        val body = Files.readString(Path.of(SEARCH_PAGE_PATH))
        val torrents = TrackerParser.OneThreeThree().parseSearch(body)

        assertThat(torrents.torrents).containsExactly(SEARCH_EXPECTED_TORRENT)
    }

    @Test
    fun `should parse torrent page 1337x`() {
        val body = Files.readString(Path.of(TORRENT_PAGE_PATH))
        val torrent = TrackerParser.OneThreeThree().parseTorrentPage(body)

        assertThat(torrent).isEqualTo(PAGE_EXPECTED_TORRENT)
    }

}