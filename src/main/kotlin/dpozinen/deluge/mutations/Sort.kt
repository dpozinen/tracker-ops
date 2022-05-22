package dpozinen.deluge.mutations

import dpozinen.deluge.DelugeState
import dpozinen.deluge.DelugeTorrent
import dpozinen.deluge.mutations.By.*
import dpozinen.deluge.mutations.Sort.Order.ASC
import dpozinen.deluge.mutations.Sort.Order.DESC

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
            NAME -> comparator(By.name)
            STATE -> comparator(By.state)
            SIZE -> comparator(By.size)
            PROGRESS -> comparator(By.progress)
            DOWNLOADED -> comparator(By.downloaded)
            RATIO -> comparator(By.ratio)
            UPLOADED -> comparator(By.uploaded)
            ETA -> comparator(By.eta)
            DATE -> comparator(By.date)
            DOWNLOAD_SPEED -> comparator(By.downloadSpeed)
            UPLOAD_SPEED -> comparator(By.uploadSpeed)
        }

    private fun <C : Comparable<C>> comparator(comparator: ByComparable<C>)=
        compareBy<DelugeTorrent> { comparator.comparable(it.getterBy(by).call(it)) }

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

        if (by != other.by) return false

        return true
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