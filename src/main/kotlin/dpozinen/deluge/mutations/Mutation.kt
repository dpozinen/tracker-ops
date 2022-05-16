package dpozinen.deluge.mutations

import dpozinen.deluge.DelugeState

fun interface Mutation {

    fun perform(state: DelugeState): DelugeState

    fun applySelf(state: DelugeState): MutableSet<Mutation> {
        val mutations = state.mutations
        mutations.add(this)
        return mutations
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
