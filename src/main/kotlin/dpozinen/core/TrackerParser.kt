package dpozinen.core

import dpozinen.model.Torrent
import dpozinen.model.Torrents
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

interface TrackerParser {

    fun parseSearch(body: String): Torrents
    fun parseTorrentPage(body: String): Torrent

    class OneThreeThree : TrackerParser {

        override fun parseSearch(body: String): Torrents {
            val document = Jsoup.parse(body)

            if (document.select("p").any { e -> e.text().contains("No results were returned") })
                return Torrents.empty()

            return Torrents(
                document.select(".table-list-wrap tbody tr")
                    .map { toTorrent(it) }
                    .toList()
            )
        }

        private fun toTorrent(element: Element): Torrent {
            val link = element.select("a[href^=/torrent/]").attr("href")
            val name = element.select("a[href^=/torrent/]").text()
            val seeds = element.select(".seeds").first()!!.text().toInt()
            val leeches = element.select(".leeches").text().toInt()
            val date = element.select(".coll-date").text()
            val size = element.select(".size").text()
            val contributor = element.select(".vip").text()

            return Torrent(link, name, size, seeds, leeches, date, contributor)
        }

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