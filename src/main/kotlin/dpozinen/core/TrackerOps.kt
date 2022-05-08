package dpozinen.core

import dpozinen.model.Torrent
import org.jsoup.Connection
import org.jsoup.Jsoup

interface TrackerOps {

    fun open(torrent: Torrent): String
    fun search(keywords: List<String>): String
    fun expandUrl(url: String): String

    class OneThreeThree(
        private val baseUrl: String
    ) : TrackerOps {

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

    companion object {
        val session: Connection = Jsoup.newSession()
    }

}