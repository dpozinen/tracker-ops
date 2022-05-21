package dpozinen.deluge.mutations

import dpozinen.deluge.DelugeState

class Filter(private val by: By, private val value: Any?, private var not: Boolean = false) : Mutation {

    override fun perform(state: DelugeState): DelugeState {
        val mutations = addSelf(state)

        val filteredTorrents = if (not) {
            state.torrents.filterNot { it.getterBy<Comparable<*>>(by).call(it) == value }
        } else {
            state.torrents.filter { it.getterBy<Comparable<*>>(by).call(it) == value }
        }

        return state.with(filteredTorrents, mutations)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Filter

        if (by != other.by) return false

        return true
    }

    override fun hashCode(): Int {
        return by.hashCode()
    }

    override fun toString(): String {
        return "Filter where $by is $value"
    }

    fun negate(): Filter {
        this.not = !not
        return this
    }

}