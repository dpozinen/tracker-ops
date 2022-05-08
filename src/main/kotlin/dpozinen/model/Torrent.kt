package dpozinen.model

import java.util.function.Function
import java.util.function.Predicate

class Torrent(
    val link: String, val name: String,
    val size: String = "",
    val seeds: Int = 0, val leeches: Int = 0,
    val date: String = "",
    val contributor: String = ""
) {

    override fun toString() = "$name - $size | s: $seeds l: $leeches"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Torrent

        if (link != other.link) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = link.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

    fun replaceMissing(other: Torrent) = Torrent(
        replace(other, { link }, { it.isEmpty() }),
        replace(other, { name }, { it.isEmpty() }),
        replace(other, { size }, { it.isEmpty() }),
        replace(other, { seeds }, { it == 0 }),
        replace(other, { leeches }, { it == 0 }),
        replace(other, { date }, { it.isEmpty() }),
        replace(other, { contributor }, { it.isEmpty() })
    )

    private fun <F> replace(other: Torrent,
                            getter: Function<Torrent, F>,
                            predicate: Predicate<F>,
    ) : F {
        val current = getter.apply(this)
        return if (predicate.test(current)) getter.apply(other) else current
    }

}