package dpozinen.deluge.core

import dpozinen.deluge.mutations.Mutation
import dpozinen.deluge.rest.clients.TorrentsResult.TorrentResult

class DelugeState(
    _torrents: List<TorrentResult> = listOf(),
    _mutations: Set<Mutation> = linkedSetOf()
) {
    val torrents: MutableList<TorrentResult> = _torrents.toMutableList()
        get() = field.toMutableList()

    val mutations: MutableSet<Mutation> = _mutations.toMutableSet()
        get() = field.toMutableSet()

    fun mutate(mutation: Mutation) = mutation.perform(this)

    fun with(mutations: Set<Mutation>) = DelugeState(torrents, mutations)

    fun with(vararg mutations: Mutation) = DelugeState(torrents, setOf(*mutations))

    fun with(torrents: List<TorrentResult>) = DelugeState(torrents, mutations)

    fun with(torrents: List<TorrentResult>, mutations: Set<Mutation>) = DelugeState(torrents, mutations)

    fun mutate(): DelugeState {
        var state = DelugeState(torrents, mutations)
        mutations.forEach {
            state = state.mutate(it)
        }
        return state
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DelugeState

        if (torrents != other.torrents) return false
        return mutations == other.mutations
    }

    override fun hashCode() = 31 * torrents.hashCode() + mutations.hashCode()

}