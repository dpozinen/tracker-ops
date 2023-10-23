package tracker

import Data
import Data.OneThreeThree
import dpozinen.tracker.TrackerParser
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Index.atIndex
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test

class ParsingTest {

    @Test
    fun `should parse search page 1337x`() {
        val body = Files.readString(Path.of(OneThreeThree.SEARCH_PAGE_PATH))
        val torrents = TrackerParser.OneThreeThree().parseSearch(body)

        assertThat(torrents.torrents).containsExactly(OneThreeThree.SEARCH_EXPECTED_TORRENT)
    }

    @Test
    fun `should parse torrent page 1337x`() {
        val body = Files.readString(Path.of(OneThreeThree.TORRENT_PAGE_PATH))
        val torrent = TrackerParser.OneThreeThree().parseTorrentPage(body)

        assertThat(torrent).isEqualTo(OneThreeThree.PAGE_EXPECTED_TORRENT)
    }

    @Test
    fun `should parse search page torrent galaxy`() {
        val body = Files.readString(Path.of(Data.TorrentGalaxy.SEARCH_PAGE_PATH))
        val torrents = TrackerParser.TorrentGalaxy().parseSearch(body)

        assertThat(torrents.torrents)
            .hasSize(15)
            .satisfies(
                { assertThat(it).isEqualTo(Data.TorrentGalaxy.SEARCH_EXPECTED_TORRENT) },
                atIndex(0))
    }

}