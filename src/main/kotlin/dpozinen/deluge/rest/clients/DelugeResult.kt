package dpozinen.deluge.rest.clients

import com.fasterxml.jackson.annotation.JsonProperty

data class DelugeResult<T>(val result: T, val error: Any?, val id: Int)

class HostsResult : MutableList<List<String>> by mutableListOf() {
    fun id() = this[0][0]
}

data class TorrentsResult(@JsonProperty("torrents") private val torrents: Map<String, TorrentResult>) {

    fun torrents(): List<TorrentResult> {
        return torrents.map { (id, torrent) -> torrent.copy(id = id) }
    }

    data class TorrentResult(
        val id: String?,
        val name: String,
        val state: String,
        val progress: Double,
        @JsonProperty("total_wanted")
        val size: Double,
        @JsonProperty("total_done")
        val downloaded: Double,
        val ratio: Double,
        @JsonProperty("total_uploaded")
        val uploaded: Double,
        @JsonProperty("download_payload_rate")
        val downloadSpeed: Double,
        val eta: Double,
        @JsonProperty("upload_payload_rate")
        val uploadSpeed: Double,
        @JsonProperty("time_added")
        val date: Long,
        @JsonProperty("download_location")
        val downloadLocation: String
    )

}