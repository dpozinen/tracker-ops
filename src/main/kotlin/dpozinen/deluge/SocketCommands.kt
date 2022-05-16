package dpozinen.deluge

import com.fasterxml.jackson.annotation.JsonProperty

interface Mutation {

    fun id(): Int = hashCode()

    fun perform(state: DelugeState): DelugeState

    fun applySelf(state: DelugeState): MutableSet<Mutation> {
        val mutations = state.mutations
        mutations.add(this)
        return mutations
    }

    class Clear(private val id: Int = 0) : Mutation {
        override fun perform(state: DelugeState): DelugeState {
            val mutations = state.mutations
            if (id == 0) mutations.clear() else mutations.remove(this)
            return state.with(mutations).mutate()
        }
    }

    class Search(@JsonProperty("name") private val name: String) : Mutation {
        override fun perform(state: DelugeState): DelugeState {
            return if (name.isEmpty()) {
                state
            } else {
                val filteredTorrents = state.torrents.asSequence()
                    .filter { nameContains(it) }
                    .toList()

                state.with(filteredTorrents, applySelf(state))
            }
        }

        private fun nameContains(it: DelugeTorrent): Boolean {
            return it.name.contains(this.name)
                    || it.name.lowercase().contains(this.name.lowercase())
                    || it.name.replace(".", " ").lowercase().contains(this.name.lowercase())
        }
    }

    class Filter(by: By, value: String)

    class Sort(private val by: By, private val order: Order = Order.ASC) : Mutation {
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
    }

    enum class By {
        NAME,
        STATE,
        SIZE,
        PROGRESS,
        DOWNLOADED,
        RATIO,
        UPLOADED,
        ETA,
        DATE,
        DOWNLOAD_SPEED {
            override fun property() = "downloadSpeed"
        },
        UPLOAD_SPEED {
            override fun property() = "uploadSpeed"
        };

        open fun property() = name.lowercase()
    }
}
