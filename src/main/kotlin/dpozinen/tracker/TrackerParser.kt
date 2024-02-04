package dpozinen.tracker

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import mu.KotlinLogging.logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URLEncoder
import java.nio.charset.Charset

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
            element.select(".size .seeds").remove()

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

    class TorrentGalaxy : TrackerParser {

        private val log = logger {}

        override fun parseSearch(body: String): Torrents {
            val document = Jsoup.parse(body)

            if (document.select("p").any { e -> e.text().contains("No results were returned") })
                return Torrents.empty()

            return Torrents(
                document.select(".tgxtablerow")
                    .map { toTorrent(it) }
                    .toList()
            )
        }

        private fun toTorrent(element: Element): Torrent {
            val link = element.select("a[href^=magnet:]").attr("href")
            val name = element.select("div[data-href^=/torrent/] b").text()

            val details: Element = element.select(".tgxtablecell tbody tr")
                .firstOrNull { it.select("td").text().contains("Size") }
                ?: run {
                    log.warn { "Torrent named $name doesn't have any details" }
                    Element("")
                }

            val size = details.select("td").first { it.text().contains("Size") }.select("span").text()
            val contributor = details.select(".username").first()?.text() ?: ""

            val seedLeech = details.select("span[title=Seeders/Leechers]")
            val seeds = seedLeech.select("b").first()?.text()?.toInt() ?: 0
            val leeches = seedLeech.select("b").last()?.text()?.toInt() ?: 0
            val date = details.select("td").first { it.text().contains("Added") }
                ?.select("small")?.text() ?: ""

            return Torrent(link, name, size, seeds, leeches, date, contributor)
        }

        override fun parseTorrentPage(body: String) = Torrent("", "") // noop, all info from search

    }

    class Trunk : TrackerParser {
        private val mapper: JsonMapper = jacksonMapperBuilder()
            .configure(FAIL_ON_UNKNOWN_PROPERTIES, false)
            .build()

        private val ref = object : TypeReference<List<Torrent>>() {}

        override fun parseSearch(body: String): Torrents {
            val torrents = mapper.readValue(body, ref)
                .map { it.copy(link = toMagnet(it), name = it.name.replace("-", " ")) }
            return Torrents(torrents)
        }

        private fun toMagnet(torrent: Torrent): String {
            val name = URLEncoder.encode(torrent.name.replace(" ", "."), Charset.defaultCharset())
            return "magnet:?xt=urn:btih:${torrent.link}&dn=$name"
        }

        override fun parseTorrentPage(body: String) = Torrent(name = "")
    }
}