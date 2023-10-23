package dpozinen.tracker

import org.jsoup.Connection
import org.jsoup.Jsoup

interface TrackerOps {

    fun open(torrent: Torrent): String
    fun search(keywords: List<String>): String
    fun expandUrl(url: String): String

    class OneThreeThree : TrackerOps {
        private val baseUrl: String = "https://1337x.to"

        override fun open(torrent: Torrent): String =
            session.newRequest()
                .url("$baseUrl${torrent.link}")
                .execute()
                .body()

        override fun search(keywords: List<String>): String =
            session.newRequest()
                .url("$baseUrl/search/${keywordsSegment(keywords)}/1/")
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

    companion object {
        val session: Connection = Jsoup.newSession()
    }

}

fun keywordsSegment(keywords: List<String>) = keywords.joinToString("+")