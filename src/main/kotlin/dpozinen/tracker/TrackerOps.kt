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
                .headers(mapOf(
                    "Referer" to "https://rarbg2021.org",
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36",
                    "Cookie" to "gaDts48g=q8h5pp9t; tcc; aby=2; ppu_main_9ef78edf998c4df1e1636c9a474d9f47=1; ppu_sub_9ef78edf998c4df1e1636c9a474d9f47=3; skt=izegwjyodt; skt=izegwjyodt; gaDts48g=q8h5pp9t; __cf_bm=YxPZVni6m2kjxfO08ACgWstKkmesN63D8AV0Cwv.W9E-1663500834-0-AZupSp29iD+HxAJ7SUf5ZEHe1YPGh6EV7egcPQmsd+MkCvwP34Vyz8v9413FNKM8fOvaU9b9CCGg6hMEzABBvFqkzDyhDv+VybcQudfDXzUHWqDem5GtVA1x+mVs59Oggg==",
                    "Accept-Language" to "uk-UA,uk;q=0.9,en-US;q=0.8,en;q=0.7",
                    "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
                    "authority" to " rarbg2021.org",
                    "upgrade-insecure-requests" to "1"
                ))
                .execute()
                .body()

        override fun search(keywords: List<String>): String =
            session.newRequest()
                .url("$baseUrl/torrents.php?search=${keywordsSegment(keywords)}&category%5B%5D=17&category%5B%5D=44&category%5B%5D=45&category%5B%5D=47&category%5B%5D=50&category%5B%5D=51&category%5B%5D=52&category%5B%5D=42&category%5B%5D=46&category%5B%5D=54&category%5B%5D=18&category%5B%5D=41&category%5B%5D=49")
                .headers(mapOf(
                    "Referer" to "https://rarbg2021.org",
                    "User-Agent" to "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36",
                    "Cookie" to "gaDts48g=q8h5pp9t; tcc; aby=2; ppu_main_9ef78edf998c4df1e1636c9a474d9f47=1; ppu_sub_9ef78edf998c4df1e1636c9a474d9f47=3; skt=izegwjyodt; skt=izegwjyodt; gaDts48g=q8h5pp9t; __cf_bm=YxPZVni6m2kjxfO08ACgWstKkmesN63D8AV0Cwv.W9E-1663500834-0-AZupSp29iD+HxAJ7SUf5ZEHe1YPGh6EV7egcPQmsd+MkCvwP34Vyz8v9413FNKM8fOvaU9b9CCGg6hMEzABBvFqkzDyhDv+VybcQudfDXzUHWqDem5GtVA1x+mVs59Oggg==",
                    "Accept-Language" to "uk-UA,uk;q=0.9,en-US;q=0.8,en;q=0.7",
                    "Accept" to "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9",
                    "authority" to " rarbg2021.org",
                    "upgrade-insecure-requests" to "1"
                ))
                .execute()
                .body()

        override fun expandUrl(url: String): String = "$baseUrl/$url"

        private fun keywordsSegment(keywords: List<String>) = keywords.joinToString("+")

    }

    companion object {
        val session: Connection = Jsoup.newSession()
    }

}