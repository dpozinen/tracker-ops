package dpozinen.deluge.rest

import com.fasterxml.jackson.databind.ObjectMapper

class DelugeParams(params: Collection<Any>) : ArrayList<Any>(params) {

    override fun toString(): String = mapper.writeValueAsString(this)

    companion object {

        private val mapper: ObjectMapper = ObjectMapper()

        fun torrents(ids: Set<String> = setOf("ALL")): DelugeParams {
            return if (ids == setOf("ALL")) torrentsParams
            else DelugeParams(listOf(torrentFields, mapOf("id" to ids)))
        }

        fun addMagnet(magnet: String, downloadFolder: String) = DelugeParams(
            listOf(
                magnet,
                mapOf("download_location" to downloadFolder)
            )
        )

        fun empty() = DelugeParams(listOf())

        fun connect(id: String) = DelugeParams(listOf(id))
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

private val torrentsParams = DelugeParams(
    listOf(torrentFields, mapOf<Any, Any>())
)
