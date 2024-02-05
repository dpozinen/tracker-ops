package dpozinen.tracker

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES
import com.fasterxml.jackson.databind.json.JsonMapper
import com.fasterxml.jackson.module.kotlin.jacksonMapperBuilder
import mu.KotlinLogging.logger
import org.jsoup.Jsoup
import org.jsoup.nodes.Element
import java.net.URLEncoder
import java.nio.charset.Charset.defaultCharset
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi

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

        @OptIn(ExperimentalEncodingApi::class)
        private fun toMagnet(torrent: Torrent): String {
            val name = URLEncoder.encode(torrent.name.replace(" ", "."), defaultCharset())
            val announce = trackers.split("\n")
                .map { String(Base64.Mime.decode(it)) }
                .joinToString(separator = "&tr=") { URLEncoder.encode(it, defaultCharset()) }
            return "magnet:?xt=urn:btih:${torrent.link}&dn=$name&tr=$announce"
        }

        override fun parseTorrentPage(body: String) = Torrent(name = "")
    }
}

private const val trackers: String = """
dWRwOi8vdHJhY2tlci5vcGVudHJhY2tyLm9yZzoxMzM3L2Fubm91bmNl
dWRwOi8vdHJhY2tlci5kbGVyLmNvbTo2OTY5L2Fubm91bmNl
aHR0cDovL3RyYWNrZXIuYnQ0Zy5jb206MjA5NS9hbm5vdW5jZQ==
aHR0cDovL29wZW4uYWNnbnh0cmFja2VyLmNvbTo4MC9hbm5vdW5jZQ==
dWRwOi8vdHJhY2tlcjIuZGxlci5jb206ODAvYW5ub3VuY2U=
dWRwOi8vbW92aWVzLnpzdy5jYTo2OTY5L2Fubm91bmNl
dWRwOi8vdHJhY2tlci50b3JyZW50LmV1Lm9yZzo0NTEvYW5ub3VuY2U=
aHR0cHM6Ly90ci5idXJuYWJ5aGlnaHN0YXIuY29tOjQ0My9hbm5vdW5jZQ==
dWRwOi8vb3Blbi54eHRvci5jb206MzA3NC9hbm5vdW5jZQ==
aHR0cHM6Ly90cmFja2VyLmxvbGlnaXJsLmNuOjQ0My9hbm5vdW5jZQ==
aHR0cHM6Ly90cmFja2VyMS41MjAuanA6NDQzL2Fubm91bmNl
dWRwOi8vdXBsb2Fkcy5nYW1lY29hc3QubmV0OjY5NjkvYW5ub3VuY2U=
aHR0cHM6Ly90cmFja2VyLnRhbWVyc3VuaW9uLm9yZzo0NDMvYW5ub3VuY2U=
dWRwOi8vdHJhY2tlci5lZGtqLmNsdWI6Njk2OS9hbm5vdW5jZQ==
aHR0cDovL3AycC4wZy5jeDo2OTY5L2Fubm91bmNl
aHR0cHM6Ly90cmFja2Vycy5tbHN1Yi5uZXQ6NDQzL2Fubm91bmNl
dWRwOi8vb3BlbnRyYWNrZXIuaW86Njk2OS9hbm5vdW5jZQ==
aHR0cDovL3RyYWNrZXIucmVuZmVpLm5ldDo4MDgwL2Fubm91bmNl
dWRwOi8vb2guZnV1dXV1Y2suY29tOjY5NjkvYW5ub3VuY2U=
dWRwOi8vZWMyLTE4LTE5MS0xNjMtMjIwLnVzLWVhc3QtMi5jb21wdXRlLmFtYXpvbmF3cy5jb206Njk2OS9hbm5vdW5jZQ==
dWRwOi8vdHRrMi5uYmFvbmxpbmVzZXJ2aWNlLmNvbTo2OTY5L2Fubm91bmNl
dWRwOi8veS5wYXJhbm9pZC5hZ2VuY3k6Njk2OS9hbm5vdW5jZQ==
aHR0cHM6Ly90cmFja2VyLnllbWVreWVkaW0uY29tOjQ0My9hbm5vdW5jZQ==
dWRwOi8vdHJhY2tlci50b3JydXN0LWRlbW8uY29tOjY5NjkvYW5ub3VuY2U=
dWRwOi8vbWFydGluLWdlYmhhcmR0LmV1OjI1L2Fubm91bmNl
aHR0cDovL2Fib3V0YmVhdXRpZnVsZ2FsbG9waW5naG9yc2VzaW50aGVncmVlbnBhc3R1cmUub25saW5lOjgwL2Fubm91bmNl
aHR0cHM6Ly9zaGFoaWRyYXppLm9ubGluZTo0NDMvYW5ub3VuY2U=
dWRwOi8vdHJhY2tlci5waWNvdG9ycmVudC5vbmU6Njk2OS9hbm5vdW5jZQ==
dWRwOi8vb3Blbi5zdGVhbHRoLnNpOjgwL2Fubm91bmNl
aHR0cDovL3RyYWNrZXIuZmlsZXMuZm06Njk2OS9hbm5vdW5jZQ==
dWRwOi8vbW9vbmJ1cnJvdy5jbHViOjY5NjkvYW5ub3VuY2U=
dWRwOi8vdHJhY2tlci5jdWJvbmVncm8ubG9sOjY5NjkvYW5ub3VuY2U=
dWRwOi8vbnMxLm1vbm9saXRoaW5kdXN0cmllcy5jb206Njk2OS9hbm5vdW5jZQ==
dWRwOi8vdHJhY2tlci4weDdjMC5jb206Njk2OS9hbm5vdW5jZQ==
dWRwOi8vdHJhY2tlci50aGVyYXJiZy5jb206Njk2OS9hbm5vdW5jZQ==
dWRwOi8vdHJhY2tlci5mbml4Lm5ldDo2OTY5L2Fubm91bmNl
aHR0cHM6Ly90ci5xZnJ1aXRpLmNvbTo0NDMvYW5ub3VuY2U=
aHR0cHM6Ly90ci5xZnJ1aXRpLmluOjQ0My9hbm5vdW5jZQ==
aHR0cHM6Ly93d3cucGVja3NlcnZlcnMuY29tOjk0NDMvYW5ub3VuY2U=
dWRwOi8vb2RkLWhkLmZyOjY5NjkvYW5ub3VuY2U=
aHR0cDovL2J0Lm9rbXAzLnJ1OjI3MTAvYW5ub3VuY2U=
aHR0cDovL2J2YXJmLnRyYWNrZXIuc2g6MjA4Ni9hbm5vdW5jZQ==
dWRwOi8vdHJhY2tlci5maWxlbWFpbC5jb206Njk2OS9hbm5vdW5jZQ==
dWRwOi8vZDQwOTY5LmFjb2QucmVncnVjb2xvLnJ1OjY5NjkvYW5ub3VuY2U=
aHR0cDovL3QuYWNnLnJpcDo2Njk5L2Fubm91bmNl
dWRwOi8vdHJhY2tlci50cnloYWNreC5vcmc6Njk2OS9hbm5vdW5jZQ==
aHR0cHM6Ly90MS5obG9saS5vcmc6NDQzL2Fubm91bmNl
dWRwOi8vd3d3LnRvcnJlbnQuZXUub3JnOjQ1MS9hbm5vdW5jZQ==
dWRwOi8vYnQxLmFyY2hpdmUub3JnOjY5NjkvYW5ub3VuY2U=
dWRwOi8vZXBpZGVyLm1lOjY5NjkvYW5ub3VuY2U=
dWRwOi8vdHJhY2tlci5taXJyb3JiYXkub3JnOjY5NjkvYW5ub3VuY2U=
dWRwOi8vYXBpLmFsYXJtYXNxdWVyZXRhcm8uY29tOjMwNzQvYW5ub3VuY2U=
aHR0cDovL3RyYWNrZXIubXl3YWlmdS5iZXN0OjY5NjkvYW5ub3VuY2U=
dWRwOi8vdGhpbmtpbmcuZHVja2Rucy5vcmc6Njk2OS9hbm5vdW5jZQ==
dWRwOi8vdHJhY2tlci50LXJiLm9yZzo2OTY5L2Fubm91bmNl
dWRwOi8vZXhvZHVzLmRlc3luYy5jb206Njk2OS9hbm5vdW5jZQ==
aHR0cHM6Ly90cmFja2VyLmxpbGl0aHJhd3MuY2Y6NDQzL2Fubm91bmNl
dWRwOi8vcmV0cmFja2VyLmxhbnRhLm1lOjI3MTAvYW5ub3VuY2U=
aHR0cHM6Ly90cmFja2VyLmxpbGl0aHJhd3Mub3JnOjQ0My9hbm5vdW5jZQ==
dWRwOi8vMWMucHJlbWllcnphbC5ydTo2OTY5L2Fubm91bmNl
dWRwOi8vZXhwbG9kaWUub3JnOjY5NjkvYW5ub3VuY2U=
dWRwOi8vNmFoZGR1dGIxdWNjM2NwLnJ1OjY5NjkvYW5ub3VuY2U=
dWRwOi8vdHJhY2tlci5xdS5heDo2OTY5L2Fubm91bmNl
dWRwOi8vdGFtYXMzLnluaC5mcjo2OTY5L2Fubm91bmNl
dWRwOi8vdTQudHJha3guY3JpbS5pc3Q6MTMzNy9hbm5vdW5jZQ==
dWRwOi8vc3UtZGF0YS5jb206Njk2OS9hbm5vdW5jZQ==
aHR0cHM6Ly9ib3QucG9ub21hci1zaWJpci5ydTo0NDMvYW5ub3VuY2U=
aHR0cDovL3RyYWNrZXIxLml0em14LmNvbTo4MDgwL2Fubm91bmNl
dWRwOi8vNi5wb2NrZXRuZXQuYXBwOjY5NjkvYW5ub3VuY2U=
dWRwOi8vZXZhbi5pbTo2OTY5L2Fubm91bmNl
dWRwOi8vYml0dG9ycmVudC10cmFja2VyLmUtbi1jLXIteS1wLXQubmV0OjEzMzcvYW5ub3VuY2U=
dWRwOi8vb3BlbnRyYWNrZXIuaTJwLnJvY2tzOjY5NjkvYW5ub3VuY2U=
dWRwOi8vdGsxLnRyYWNrZXJzZXJ2ZXJzLmNvbTo4MDgwL2Fubm91bmNl
dWRwOi8vb3Blbi5kc3R1ZC5pbzo2OTY5L2Fubm91bmNl
aHR0cHM6Ly90cmFja2VyLmdjcnJlZW4ueHl6OjQ0My9hbm5vdW5jZQ==
dWRwOi8vd2Vwem9uZS5uZXQ6Njk2OS9hbm5vdW5jZQ==
dWRwOi8vdHJhY2tlci5zcnYwMC5jb206Njk2OS9hbm5vdW5jZQ==
aHR0cHM6Ly90cmFja2VyLmNsb3VkaXQudG9wOjQ0My9hbm5vdW5jZQ==
dWRwOi8vYnQyLmFyY2hpdmUub3JnOjY5NjkvYW5ub3VuY2U=
aHR0cDovL3RyYWNrZXIuZ2JpdHQuaW5mbzo4MC9hbm5vdW5jZQ==
dWRwOi8vdHJhY2tlci11ZHAuZ2JpdHQuaW5mbzo4MC9hbm5vdW5jZQ==
dWRwOi8vbmV3LWxpbmUubmV0OjY5NjkvYW5ub3VuY2U=
dWRwOi8vdHJhY2tlci50aGVyYXJiZy50bzo2OTY5L2Fubm91bmNl
dWRwOi8vYmxhY2stYmlyZC55bmguZnI6Njk2OS9hbm5vdW5jZQ==
dWRwOi8vdHJhY2tlci5hbmltYS5uejo2OTY5L2Fubm91bmNl"""
