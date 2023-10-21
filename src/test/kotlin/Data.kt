
import dpozinen.deluge.domain.DelugeTorrent
import dpozinen.deluge.domain.DelugeTorrents
import dpozinen.deluge.rest.clients.TorrentsResult
import dpozinen.tracker.Torrent
import org.springframework.http.HttpHeaders
import java.net.HttpCookie

class Data {

    class OneThreeThree {
        companion object {
            const val SEARCH_PAGE_PATH = "src/test/resources/one337xto/search.html"
            const val TORRENT_PAGE_PATH = "src/test/resources/one337xto/torrent-page.html"

            val PAGE_EXPECTED_TORRENT = Torrent(
                "magnet:?xt=urn:btih:7F7369A43153DEB61017E4E3076DD4DF6AED6F5F&dn=The+Birdcage+%281996%29+%281080p+BluRay+x265+HEVC+10bit+AAC+5.1+Panda%29+%5BQxR%5D&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.torrent.eu.org%3A451%2Fannounce&tr=+udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.zer0day.to%3A1337%2Fannounce&tr=udp%3A%2F%2Feddie4.nl%3A6969%2Fannounce+&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=http%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=udp%3A%2F%2Fopentracker.i2p.rocks%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.internetwarriors.net%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969%2Fannounce&tr=udp%3A%2F%2Fcoppersurfer.tk%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.zer0day.to%3A1337%2Fannounce",
                "The Birdcage (1996) (1080p BluRay x265 HEVC 10bit AAC 5.1 Panda) ..."
            )

            val PAGE_COMPLETE_EXPECTED_TORRENT = Torrent(
                "magnet:?xt=urn:btih:7F7369A43153DEB61017E4E3076DD4DF6AED6F5F&dn=The+Birdcage+%281996%29+%281080p+BluRay+x265+HEVC+10bit+AAC+5.1+Panda%29+%5BQxR%5D&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.torrent.eu.org%3A451%2Fannounce&tr=+udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.zer0day.to%3A1337%2Fannounce&tr=udp%3A%2F%2Feddie4.nl%3A6969%2Fannounce+&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=http%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=udp%3A%2F%2Fopentracker.i2p.rocks%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.internetwarriors.net%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969%2Fannounce&tr=udp%3A%2F%2Fcoppersurfer.tk%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.zer0day.to%3A1337%2Fannounce",
                "The Birdcage (1996) (1080p BluRay x265 HEVC 10bit AAC 5.1 Panda) ...",
                "3.9 GB",
                22,
                6,
                "Jan. 7th '19",
                "QxR"
            )

            val SEARCH_EXPECTED_TORRENT = Torrent(
                "/torrent/3507859/The-Birdcage-1996-1080p-BluRay-x265-HEVC-10bit-AAC-5-1-Panda-QxR/",
                "The Birdcage (1996) (1080p BluRay x265 HEVC 10bit AAC 5.1 Panda) [QxR]",
                "3.9 GB",
                22,
                6,
                "Jan. 7th '19",
                "QxR"
            )
        }
    }


    companion object {
        val info: DelugeTorrents.Info = DelugeTorrents.Info(
            selected = 100,
            total = 100,
            downloading = 0,
            paused = 0,
            seeding = 100,
            activeDown = 0,
            activeUp = 100,
            uploaded = "53.8 TiB",
            ratio = 67.9,
            downloaded = "811.39 GiB",
            downSpeed = "",
            upSpeed = "17.58 KiB/s"
        )

        val delugeTorrentResponse = TorrentsResult.TorrentResult(
            id = "ee21ac410a4df9d2a09a97a6890fc74c0d143a0b",
            name = "Rick and Morty Season 1  [2160p AI x265 FS100 Joy]",
            state = "Seeding",
            progress = 100.0,
            size = 8712212443.0,
            downloaded = 8712212443.0,
            ratio = 67.9033203125,
            uploaded = 591588139506.0,
            downloadSpeed = 0.0,
            eta = 72600.0,
            uploadSpeed = 180.0,
            date = 1624829185,
            downloadLocation = "/Downloads/Show",
        )

        val delugeTorrent = DelugeTorrent(
            id = "ee21ac410a4df9d2a09a97a6890fc74c0d143a0b",
            name = "Rick and Morty Season 1  [2160p AI x265 FS100 Joy]",
            state = "Seeding",
            progress = "100.0",
            size = "8.11 GiB",
            ratio = "67.9",
            uploaded = "550.96 GiB",
            downloaded = "8.11 GiB",
            eta = "20h 10m",
            downloadSpeed = "",
            uploadSpeed = "0.18 KiB/s",
            date = "28.06.2021"
        )

        const val sessionIdCookie =
            "_session_id=ff9533d4210cb1ebb0062b193f02234a1a7da716b2f7ce7fbea417cd3b35eb954594; Expires=Thu, 12 May 2022 16:11:28 GMT"

        val sessionIdHttpCookie: HttpCookie = HttpCookie.parse(
            sessionIdCookie
                .substringBefore("Expires")
                .plus("max-age=3500")
        )[0]

        fun httpHeaders(): HttpHeaders {
            val httpHeaders = HttpHeaders()
            httpHeaders["Set-Cookie"] = sessionIdCookie
            return httpHeaders
        }

    }

}

