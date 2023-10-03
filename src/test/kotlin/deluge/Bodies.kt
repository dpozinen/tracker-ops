package deluge

import dpozinen.deluge.rest.clients.TorrentsResult
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.atIndex

class Bodies {

    companion object {
        const val loginRequest = """
            {
                "method" : "auth.login",
                "params" : [ "deluge" ],
                "id" : 8888
            }
            """

        const val loginResponse = """
            {
                "result": true,
                "error": null,
                "id": 8888
            }
            """

        const val hostsRequest = """
            {
                "method": "web.get_hosts",
                "params": [],
                "id": 8888
            }
            """

        const val hostsResponse = """
            {
                "result": [
                    [
                        "8c0f8366771f4859a48d7cc61dbd9e1f",
                        "127.0.0.1",
                        58846,
                        "localclient"
                    ]
                ],
                "error": null,
                "id": 8888
            }
            """
        const val connectRequest = """
            {
                "method": "web.connect",
                "params": [ "8c0f8366771f4859a48d7cc61dbd9e1f" ],
                "id": 8888
            }
            """

        const val connectResponse = """
            {
                "result": [],
                "error": null,
                "id": 8888
            }"""

        const val disconnectedResponse = """
            {
                "result": {
                    "connected": false,
                    "torrents": null,
                    "filters": null,
                    "stats": {
                        "max_download": null,
                        "max_upload": null,
                        "max_num_connections": null
                    }
                },
                "error": null,
                "id": 8888
            }
            """

        const val moveRequest = """
            {
                "method": "core.move_storage",
                "params": [
                    [
                        "ee21ac410a4df9d2a09a97a6890fc74c0d143a0b",
                        "776551013d0d91c1d58674be34ebff91ec0c4b94"
                    ],
                    "/Downloads/Show"
                ],
                "id": 8888
            }
            """

        const val moveResponse = """ {"result": null, "error": null, "id": 8888} """

        const val addMagnetRequest = """
            {
                "method": "core.add_torrent_magnet",
                "params": [ "magnet", {"download_location" : "folder"} ],
                "id": 8888
            }
            """

        const val addMagnetResponse = """ {"result": "28b9885fdcd79be910863837e72978f7e1aa0991", "error": null, "id": 8888} """

        fun stringResource(path: String) = this::class.java.getResource(path)?.readText(Charsets.UTF_8)!!
    }

}

fun assertTorrents(torrents: List<TorrentsResult.TorrentResult>) {
    assertThat(torrents).hasSize(1).satisfies({ torrent ->
        assertThat(torrent.id).isEqualTo("ee21ac410a4df9d2a09a97a6890fc74c0d143a0b")
        assertThat(torrent.name).isEqualTo("Rick and Morty Season 1  [2160p AI x265 FS100 Joy]")
        assertThat(torrent.state).isEqualTo("Paused")
        assertThat(torrent.progress).isEqualTo(100.0)
        assertThat(torrent.size).isEqualTo(8712212443.0)
        assertThat(torrent.downloaded).isEqualTo(8712212443.0)
        assertThat(torrent.ratio).isEqualTo(84.44232177734375)
        assertThat(torrent.uploaded).isEqualTo(735679472733.0)
        assertThat(torrent.downloadSpeed).isEqualTo(0.0)
        assertThat(torrent.eta).isEqualTo(0.0)
        assertThat(torrent.uploadSpeed).isEqualTo(0.0)
        assertThat(torrent.date).isEqualTo(1624829185)
        assertThat(torrent.downloadLocation).isEqualTo("/Downloads/Show")
    }, atIndex(0))
}