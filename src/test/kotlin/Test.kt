import org.assertj.core.api.Assertions.assertThat
import org.mockito.Mockito
import java.nio.file.Files
import java.nio.file.Path
import kotlin.test.Test

private const val SEARCH_PAGE_PATH = "/Users/dpozinenko/IdeaProjects/1337xto/src/test/resources/one337xto/search.html"

private const val TORRENT_PAGE_PATH = "/Users/dpozinenko/IdeaProjects/1337xto/src/test/resources/one337xto/torrent-page.html"

private val PAGE_EXPECTED_TORRENT = Torrent(
    "magnet:?xt=urn:btih:7F7369A43153DEB61017E4E3076DD4DF6AED6F5F&dn=The+Birdcage+%281996%29+%281080p+BluRay+x265+HEVC+10bit+AAC+5.1+Panda%29+%5BQxR%5D&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.torrent.eu.org%3A451%2Fannounce&tr=+udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.zer0day.to%3A1337%2Fannounce&tr=udp%3A%2F%2Feddie4.nl%3A6969%2Fannounce+&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=http%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=udp%3A%2F%2Fopentracker.i2p.rocks%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.internetwarriors.net%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969%2Fannounce&tr=udp%3A%2F%2Fcoppersurfer.tk%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.zer0day.to%3A1337%2Fannounce",
    "The Birdcage (1996) (1080p BluRay x265 HEVC 10bit AAC 5.1 Panda) ..."
)

class Test {

    @Test
    fun `should get torrents`() {
        val body = Files.readString(Path.of(SEARCH_PAGE_PATH))
        val torrents = TrackerParser.OneThreeThree().parseSearch(body)

        assertThat(torrents.torrents)
            .containsExactly(
                Torrent(
                    "/torrent/3507859/The-Birdcage-1996-1080p-BluRay-x265-HEVC-10bit-AAC-5-1-Panda-QxR/",
                    "The Birdcage (1996) (1080p BluRay x265 HEVC 10bit AAC 5.1 Panda) [QxR]"
                )
            )
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