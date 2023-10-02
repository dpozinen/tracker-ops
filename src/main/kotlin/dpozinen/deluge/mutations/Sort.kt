package dpozinen.deluge.mutations

import dpozinen.deluge.core.DelugeState
import dpozinen.deluge.mutations.By.DATE
import dpozinen.deluge.mutations.By.NAME
import dpozinen.deluge.mutations.By.STATE
import dpozinen.deluge.mutations.Sort.Order.ASC
import dpozinen.deluge.mutations.Sort.Order.DESC
import dpozinen.deluge.rest.clients.TorrentsResult.TorrentResult

class Sort(private val by: By, private var order: Order = ASC) : Mutation {
    enum class Order { ASC, DESC }

    override fun perform(state: DelugeState): DelugeState {
        val mutations = addSelf(state)

        val combinedComparator = mutations
            .filterIsInstance<Sort>()
            .map {
                if (order == ASC) it.comparator() else it.comparator().reversed()
            }.reduce { acc, sort -> acc.thenComparing(sort) }

        val sortedTorrents = state.torrents.sortedWith(combinedComparator)

        return state.with(sortedTorrents, mutations)
    }

    private fun comparator() =
        when (by) {
            NAME, STATE -> typedComparator<String>()
            DATE -> typedComparator<Long>()
            else -> typedComparator<Double>()
        }

    private fun <C : Comparable<C>> typedComparator() =
        compareBy<TorrentResult> { it.getterBy<C>(by).call(it) }

    fun reverse(): Sort {
        when (this.order) {
            ASC -> this.order = DESC
            DESC -> this.order = ASC
        }
        return this
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sort

        return by == other.by
    }

    override fun hashCode() = by.hashCode()

    override fun toString() = "Sort by $by in $order order"


    class Reverse(private val sort: Sort) : Mutation {

        override fun perform(state: DelugeState): DelugeState {
            state.mutations
                .filterIsInstance<Sort>()
                .firstOrNull { it == sort }?.reverse()

            return state
        }

        override fun toString() = "Reverse: $sort"

    }
}