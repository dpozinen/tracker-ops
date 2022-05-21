package dpozinen.deluge.mutations

import dpozinen.deluge.DelugeState

fun interface Mutation {

    fun perform(state: DelugeState): DelugeState

    fun addSelf(state: DelugeState): MutableSet<Mutation> {
        val mutations = state.mutations
        mutations.add(this)
        return mutations
    }

}
