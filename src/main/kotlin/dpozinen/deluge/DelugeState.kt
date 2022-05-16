package dpozinen.deluge

import dpozinen.deluge.mutations.Mutation

class DelugeState(
    _torrents: List<DelugeTorrent> = listOf(),
    _mutations: Set<Mutation> = linkedSetOf()
) {
    val torrents: MutableList<DelugeTorrent> = _torrents.toMutableList()
        get() = field.toMutableList()

    val mutations: MutableSet<Mutation> = _mutations.toMutableSet()
        get() = field.toMutableSet()

    fun mutate(mutation: Mutation): DelugeState {
        return mutation.perform(this)
    }

    fun with(mutations: Set<Mutation>): DelugeState {
        return DelugeState(torrents, mutations)
    }

    fun with(torrents: List<DelugeTorrent>): DelugeState {
        return DelugeState(torrents, mutations)
    }

    fun with(torrents: List<DelugeTorrent>, mutations: Set<Mutation>): DelugeState {
        return DelugeState(torrents, mutations)
    }

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
        if (mutations != other.mutations) return false

        return true
    }

    override fun hashCode(): Int {
        var result = torrents.hashCode()
        result = 31 * result + mutations.hashCode()
        return result
    }


}