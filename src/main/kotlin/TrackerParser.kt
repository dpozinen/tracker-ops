import org.jsoup.Jsoup

interface TrackerParser {

    fun parseSearch(body: String): Torrents
    fun parseTorrentPage(body: String): Torrent

    class OneThreeThree : TrackerParser {

        override fun parseSearch(body: String): Torrents =
            Torrents(
                Jsoup.parse(body)
                    .select(".table-list-wrap tbody a[href^=/torrent/]")
                    .map { Torrent.from(it) }
                    .toList()
            )

        override fun parseTorrentPage(body: String): Torrent {
            val document = Jsoup.parse(body)

            val link = document
                .select(".box-info a[href^=magnet]")
                .first()!!
                .attr("href")

            val name = document
                .select("h1")
                .first()!!
                .text()

            return Torrent(link, name)
        }

    }

}