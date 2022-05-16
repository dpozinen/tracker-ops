package dpozinen.deluge.mutations

import dpozinen.deluge.DelugeState

class Clear(private val mutation: Mutation? = null) : Mutation {
    override fun perform(state: DelugeState): DelugeState {
        val mutations = state.mutations

        if (mutation == null) mutations.clear()
        else mutations.remove(mutation)

        return state.with(mutations).mutate()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Clear

        if (mutation != other.mutation) return false

        return true
    }

    override fun hashCode(): Int {
        return mutation?.hashCode() ?: 0
    }


}