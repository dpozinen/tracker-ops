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

        private fun keywordsSegment(keywords: List<String>) = keywords.joinToString("+")

    }

    class Rarbg : TrackerOps {
        private val baseUrl: String = "https://rarbg.to"

        override fun open(torrent: Torrent): String =
            session.newRequest()
                .url("$baseUrl${torrent.link}")
                .execute()
                .body()

        override fun search(keywords: List<String>): String =
            session.newRequest()
                .url("$baseUrl/torrents.php?search=${keywordsSegment(keywords)}&category%5B%5D=17&category%5B%5D=44&category%5B%5D=45&category%5B%5D=47&category%5B%5D=50&category%5B%5D=51&category%5B%5D=52&category%5B%5D=42&category%5B%5D=46&category%5B%5D=54&category%5B%5D=18&category%5B%5D=41&category%5B%5D=49")
                .execute()
                .body()

        override fun expandUrl(url: String): String = "$baseUrl/$url"

        private fun keywordsSegment(keywords: List<String>) = keywords.joinToString("+")

    }

    companion object {
        val session: Connection = Jsoup.newSession()
    }

}