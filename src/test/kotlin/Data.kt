import dpozinen.model.Torrent

class Data {

    companion object {
        const val SEARCH_PAGE_PATH = "/Users/dpozinenko/IdeaProjects/1337xto/src/test/resources/one337xto/search.html"
        const val TORRENT_PAGE_PATH = "/Users/dpozinenko/IdeaProjects/1337xto/src/test/resources/one337xto/torrent-page.html"

        val PAGE_EXPECTED_TORRENT = Torrent(
            "magnet:?xt=urn:btih:7F7369A43153DEB61017E4E3076DD4DF6AED6F5F&dn=The+Birdcage+%281996%29+%281080p+BluRay+x265+HEVC+10bit+AAC+5.1+Panda%29+%5BQxR%5D&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.torrent.eu.org%3A451%2Fannounce&tr=+udp%3A%2F%2Ftracker.coppersurfer.tk%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.zer0day.to%3A1337%2Fannounce&tr=udp%3A%2F%2Feddie4.nl%3A6969%2Fannounce+&tr=udp%3A%2F%2Ftracker.opentrackr.org%3A1337%2Fannounce&tr=http%3A%2F%2Ftracker.openbittorrent.com%3A80%2Fannounce&tr=udp%3A%2F%2Fopentracker.i2p.rocks%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.internetwarriors.net%3A1337%2Fannounce&tr=udp%3A%2F%2Ftracker.leechers-paradise.org%3A6969%2Fannounce&tr=udp%3A%2F%2Fcoppersurfer.tk%3A6969%2Fannounce&tr=udp%3A%2F%2Ftracker.zer0day.to%3A1337%2Fannounce",
            "The Birdcage (1996) (1080p BluRay x265 HEVC 10bit AAC 5.1 Panda) ..."
        )

        val SEARCH_EXPECTED_TORRENT = Torrent(
            "/torrent/3507859/The-Birdcage-1996-1080p-BluRay-x265-HEVC-10bit-AAC-5-1-Panda-QxR/",
            "The Birdcage (1996) (1080p BluRay x265 HEVC 10bit AAC 5.1 Panda) [QxR]"
        )
    }
}

