package dpozinen.deluge.rest

import com.fasterxml.jackson.annotation.JsonValue
import dpozinen.deluge.rest.DelugeRequest.Method.add_magnet
import dpozinen.deluge.rest.DelugeRequest.Method.connect
import dpozinen.deluge.rest.DelugeRequest.Method.get_hosts
import dpozinen.deluge.rest.DelugeRequest.Method.login
import dpozinen.deluge.rest.DelugeRequest.Method.move_storage
import dpozinen.deluge.rest.DelugeRequest.Method.update_ui

data class DelugeRequest(
    val method: Method,
    val params: DelugeParams,
    val id: Int = 8888
) {
    class DelugeParams(params: Collection<Any> = listOf()) : ArrayList<Any>(params)

    enum class Method(@JsonValue val method: String) {
        login("auth.login"),
        add_magnet("core.add_torrent_magnet"),
        update_ui("web.update_ui"),
        get_hosts("web.get_hosts"),
        connect("web.connect"),
        move_storage("core.move_storage")
    }

    companion object {
        fun addMagnet(magnet: String, downloadFolder: String) = DelugeRequest(
            add_magnet,
            DelugeParams(listOf(magnet, mapOf("download_location" to downloadFolder)))
        )

        fun torrents() = DelugeRequest(update_ui, DelugeParams(listOf(torrentFields, mapOf<Any, Any>())))

        fun connect(hostId: String) = DelugeRequest(connect, DelugeParams(listOf(hostId)))

        fun hosts() = DelugeRequest(get_hosts, DelugeParams())

        fun login() = DelugeRequest(login, DelugeParams(listOf("deluge")))

        fun move(to: String, vararg ids: String) = DelugeRequest(
            move_storage,
            DelugeParams(listOf(ids, to))
        )

    }
}

private val torrentFields: Collection<String> = listOf(
    "queue",
    "name",
    "total_wanted",
    "state",
    "progress",
    "num_seeds",
    "total_seeds",
    "num_peers",
    "total_peers",
    "download_payload_rate",
    "upload_payload_rate",
    "eta",
    "ratio",
    "distributed_copies",
    "is_auto_managed",
    "time_added",
    "tracker_host",
    "download_location",
    "last_seen_complete",
    "total_done",
    "total_uploaded",
    "max_download_speed",
    "max_upload_speed",
    "seeds_peers_ratio",
    "total_remaining",
    "completed_time",
    "time_since_transfer"
)
