package dpozinen.deluge

import kotlinx.collections.immutable.toImmutableList

class DelugeState(
    _torrents: List<DelugeTorrent> = listOf(),
    _applied: Set<Mutation> = linkedSetOf()
) {
    val torrents: MutableList<DelugeTorrent> = _torrents.toMutableList()
        get() = field.toMutableList()

    val applied: MutableSet<Mutation> = _applied.toMutableSet()
        get() = field.toMutableSet()

    fun mutate(mutation: Mutation): DelugeState {
        return mutation.perform(this)
    }

    fun with(mutations: Set<Mutation>): DelugeState {
        return DelugeState(torrents, mutations)
    }

    fun with(torrents: List<DelugeTorrent>): DelugeState {
        return DelugeState(torrents.toImmutableList(), applied)
    }

    fun with(torrents: List<DelugeTorrent>, mutations: Set<Mutation>): DelugeState {
        return DelugeState(torrents.toImmutableList(), mutations)
    }

    fun mutate(): DelugeState {
        var state = DelugeState(torrents, applied)
        applied.forEach {
            state = state.mutate(it)
        }
        return state
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DelugeState

        if (torrents != other.torrents) return false
        if (applied != other.applied) return false

        return true
    }

    override fun hashCode(): Int {
        var result = torrents.hashCode()
        result = 31 * result + applied.hashCode()
        return result
    }


}