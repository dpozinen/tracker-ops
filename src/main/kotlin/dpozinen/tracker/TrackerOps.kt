package dpozinen.tracker

import com.jayway.jsonpath.Configuration
import com.jayway.jsonpath.JsonPath
import com.jayway.jsonpath.spi.mapper.JacksonMappingProvider
import org.jsoup.Connection
import org.jsoup.Jsoup
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.isRegularFile

interface TrackerOps {

    fun open(torrent: Torrent): String
    fun search(keywords: List<String>): String
    fun expandUrl(url: String): String

    class OneThreeThree : TrackerOps {
        private val cookie: String = System.getenv("ONE_THREE_THREE_COOKIE") ?: ""
        private val baseUrl: String = "https://1337x.to"

        override fun open(torrent: Torrent): String =
            session.newRequest()
                .url("$baseUrl${torrent.link}")
                .header("Cookie", cookie)
                .execute()
                .body()

        override fun search(keywords: List<String>): String =
            session.newRequest()
                .url("$baseUrl/search/${keywordsSegment(keywords)}/1/")
                .header("Cookie", cookie)
                .execute()
                .body()

        override fun expandUrl(url: String): String = "$baseUrl/$url"

    }

    class TorrentGalaxy : TrackerOps {
        private val baseUrl: String = "https://torrentgalaxy.to"

        override fun open(torrent: Torrent) = "" // noop

        override fun search(keywords: List<String>): String =
            session.newRequest()
                .url("$baseUrl/torrents.php?search=${keywordsSegment(keywords)}")
                .execute()
                .body()

        override fun expandUrl(url: String): String = "$baseUrl/$url"

    }

    class Trunk : TrackerOps {
        private val dir = Path.of(this.javaClass.getResource("/hashes")!!.toURI())
        private val jsonConfig = Configuration.builder().mappingProvider(JacksonMappingProvider()).build()

        override fun open(torrent: Torrent) = ""

        override fun search(keywords: List<String>): String {
            return Files.walk(dir)
                .parallel()
                .filter { it.isRegularFile() }
                .flatMap { collectMatches(it, keywords).stream() }
                .toList()
                .let { JsonPath.parse(it).jsonString() }
        }

        override fun expandUrl(url: String) = ""

        private fun collectMatches(path: Path, keywords: List<String>): List<String> {
            return JsonPath.parse(path.toFile(), jsonConfig)
                .read("$.*.[?]", { matches(keywords, it.item(Map::class.java)) })
        }

        private fun matches(keywords: List<String>, candidate: Map<*, *>): Boolean {
            val name = candidate["name"] as String

            return keywords.count { name.lowercase().contains(it.lowercase()) } == keywords.size
        }

    }

    companion object {
        val session: Connection = Jsoup.newSession()
    }

}

fun keywordsSegment(keywords: List<String>) = keywords.joinToString("+")