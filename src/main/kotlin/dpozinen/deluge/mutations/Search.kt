package dpozinen.deluge.mutations

import com.fasterxml.jackson.annotation.JsonProperty
import dpozinen.deluge.core.DelugeState
import dpozinen.deluge.domain.DelugeTorrent

class Search(@JsonProperty("name") val name: String) : Mutation {
    override fun perform(state: DelugeState): DelugeState {
        val filteredTorrents = state.torrents.asSequence()
            .filter { nameContains(it) }
            .toList()

        return state.with(filteredTorrents, addSelf(state))
    }

    private fun nameContains(torrent: DelugeTorrent) =
               torrent.name.contains(this.name)
            || torrent.name.lowercase().contains(this.name.lowercase())
            || torrent.name.replace(".", " ").lowercase().contains(this.name.lowercase())

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Search

        return name == other.name
    }

    override fun hashCode() = name.hashCode()

    override fun toString() = "Search for $name"

}