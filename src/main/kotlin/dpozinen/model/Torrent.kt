package dpozinen.model

data class Torrent(
    val link: String, val name: String,
    val size: String = "",
    val seeds: Int = 0, val leeches: Int = 0,
    val date: String = "",
    val contributor: String = ""
) {

    override fun toString() = "$name - $size | s: $seeds l: $leeches"

    fun replaceMissing(other: Torrent) = Torrent(
        replace(other, { it.link }, { it.isEmpty() }),
        replace(other, { it.name }, { it.isEmpty() }),
        replace(other, { it.size }, { it.isEmpty() }),
        replace(other, { it.seeds }, { it == 0 }),
        replace(other, { it.leeches }, { it == 0 }),
        replace(other, { it.date }, { it.isEmpty() }),
        replace(other, { it.contributor }, { it.isEmpty() })
    )

    private fun <F> replace(other: Torrent,
                            getter: (Torrent) -> F,
                            predicate: (F) -> Boolean,
    ) : F {
        val current = getter.invoke(this)
        return if (predicate.invoke(current)) getter.invoke(other) else current
    }

}