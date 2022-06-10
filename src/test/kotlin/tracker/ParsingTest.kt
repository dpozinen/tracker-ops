package tracker

import Data.*
import dpozinen.tracker.TrackerParser
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Index
import org.assertj.core.data.Index.atIndex
import kotlin.test.Test
import java.nio.file.Files
import java.nio.file.Path

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
    fun `should parse search page rarbg`() {
        val body = Files.readString(Path.of(Rarbg.SEARCH_PAGE_PATH))
        val torrents = TrackerParser.Rarbg().parseSearch(body)

        assertThat(torrents.torrents).contains(Rarbg.SEARCH_EXPECTED_TORRENT, atIndex(0))
    }

    @Test
    fun `should parse torrent page rarbg`() {
        val body = Files.readString(Path.of(Rarbg.TORRENT_PAGE_PATH))
        val torrent = TrackerParser.Rarbg().parseTorrentPage(body)

        assertThat(torrent).isEqualTo(Rarbg.PAGE_EXPECTED_TORRENT)
    }

}