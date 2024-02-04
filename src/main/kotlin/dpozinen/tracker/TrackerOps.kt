package dpozinen.tracker

import kotlinx.coroutines.runBlocking
import org.jsoup.Connection
import org.jsoup.Jsoup
import org.springframework.http.HttpHeaders.USER_AGENT
import org.springframework.http.MediaType.APPLICATION_JSON
import org.springframework.http.ResponseEntity
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

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

    class DigitalCore(private val baseUrl: String, cookies: String) : TrackerOps {
        private val cookies: Map<String, String>
        private val webClient: WebClient

        init {
            this.cookies = cookies.split(";").associate { it.split("=")[0] to it.split("=")[1] }
            this.webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeaders {
                    it.accept = listOf(APPLICATION_JSON)
                    it.set(USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/116.0")
                }
                .defaultCookies { this.cookies }
                .build()
        }

        override fun open(torrent: Torrent): String {
            TODO("Not yet implemented")
        }

        override fun search(keywords: List<String>): String {
            val url = "${baseUrl}/api/v1/torrents?" +
                    "categories[]=2&categories[]=1&categories[]=5&categories[]=6" +
                    "&categories[]=4&categories[]=3&categories[]=38&categories[]=7" +
                    "&categories[]=10&categories[]=11&categories[]=8&categories[]=9" +
                    "&categories[]=13&categories[]=14&categories[]=12" +
                    "&dead=false&extendedDead=false&extendedSearch=false" +
                    "&extendedTitle=false&freeleech=false" +
                    "&index=0&limit=15&order=asc&page=search&" +
                    "searchText=${keywordsSegment(keywords)}" +
                    "&section=all&sort=n&stereoscopic=false&title=false&watchview=false"

            return runBlocking {
                webClient.get()
                    .uri(url).retrieve().awaitBody<ResponseEntity<String>>().body!!
            }
        }

        override fun expandUrl(url: String): String {
            TODO("Not yet implemented")
        }

    }

    companion object {
        val session: Connection = Jsoup.newSession()
    }

}

fun keywordsSegment(keywords: List<String>) = keywords.joinToString("+")

// curl 'https://digitalcore.club/api/v1/torrents?categories%5B%5D=2&categories%5B%5D=1&categories%5B%5D=5&categories%5B%5D=6&categories%5B%5D=4&categories%5B%5D=3&categories%5B%5D=38&categories%5B%5D=7&categories%5B%5D=10&categories%5B%5D=11&categories%5B%5D=8&categories%5B%5D=9&categories%5B%5D=13&categories%5B%5D=14&categories%5B%5D=12&dead=false&extendedDead=false&extendedSearch=false&extendedTitle=false&freeleech=false&index=0&limit=15&order=asc&page=search&searchText=old&section=all&sort=n&stereoscopic=false&title=false&watchview=false'
// --compressed
// -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:109.0) Gecko/20100101 Firefox/116.0'
// -H 'Accept: application/json, text/plain, */*'
// -H 'Accept-Language: en-US,en;q=0.5'
// -H 'Accept-Encoding: gzip, deflate, br'
// -H 'Connection: keep-alive'
// -H 'Referer: https://digitalcore.club/search?search=old&cats=2,1,5,6,4,3,38,7,10,11,8,9,13,14,12&fc=true'
// -H 'Cookie: PHPSESSID=nijsifpskcc0qofi2ce17kdq55; uid=56474; pass=a9a5dff17373fa810a9c627909d6f4fb'
// -H 'Sec-Fetch-Dest: empty'
// -H 'Sec-Fetch-Mode: cors'
// -H 'Sec-Fetch-Site: same-origin'
// -H 'TE: trailers'


