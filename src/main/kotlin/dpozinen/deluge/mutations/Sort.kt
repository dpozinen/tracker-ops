package dpozinen.deluge.mutations

import dpozinen.deluge.DelugeState
import dpozinen.deluge.DelugeTorrent

class Sort(private val by: Mutation.By, private var order: Order = Order.ASC) : Mutation {
    enum class Order { ASC, DESC }

    override fun perform(state: DelugeState): DelugeState {
        val mutations = applySelf(state)

        val combinedComparator = mutations
            .filterIsInstance<Sort>()
            .map { it.comparator() }
            .reduce { acc, sort -> acc.thenComparing(sort) }

        val sortedTorrents = state.torrents.sortedWith(combinedComparator)

        return state.with(sortedTorrents, mutations)
    }

    private fun comparator(): Comparator<DelugeTorrent> {
        return if (this.order == Order.ASC) {
            compareBy { it.value(this.by) }
        } else {
            compareBy<DelugeTorrent> { it.value(this.by) }.reversed()
        }
    }

    fun reverse() {
        when (this.order) {
            Order.ASC -> this.order = Order.DESC
            Order.DESC -> this.order = Order.ASC
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Sort

        if (by != other.by) return false

        return true
    }

    override fun hashCode(): Int {
        return by.hashCode()
    }

    override fun toString(): String {
        return "Sort by $by in $order order"
    }


    class Reverse(private val sort: Sort) : Mutation {

        override fun perform(state: DelugeState): DelugeState {
            val mutations = state.mutations

            (mutations.first { it == sort } as Sort).reverse()

            return state.with(mutations)
        }

    }
}