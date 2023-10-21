package dpozinen.deluge.rest.clients

import com.fasterxml.jackson.annotation.JsonProperty
import dpozinen.deluge.domain.DelugeTorrent
import dpozinen.deluge.mutations.By
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

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
    ) {
        fun <T> getterBy(by: By) =
            this::class.memberProperties
                .filter { it.name == by.property() }
                .map { it.getter }
                .filterIsInstance<KProperty1.Getter<DelugeTorrent, T>>()
                .first()

        fun type() = TorrentType.from(name)

        enum class TorrentType {
            SHOW, FILM;
            companion object {
                private val regexS = Regex(".*S[0-9][0-9]?.*")
                private val regexSeason = Regex(".*Season ?[0-9][0-9]?.*")
                fun from(name: String) =
                    if (name.replace("FS88", "") matches(regexS) ||
                        name.replace("FS88", "") matches (regexSeason)) {
                        SHOW
                    } else FILM
            }
        }

    }

}