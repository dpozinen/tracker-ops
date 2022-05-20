package dpozinen.deluge.mutations

import dpozinen.deluge.DelugeTorrent
import dpozinen.deluge.DelugeTorrentConverter
import dpozinen.deluge.mutations.By.ByComparator
import java.time.LocalDate
import java.util.Comparator

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
    DOWNLOAD_SPEED { override fun property() = "downloadSpeed" },
    UPLOAD_SPEED { override fun property() = "uploadSpeed" };

    open fun property() = name.lowercase()

    fun comparator(): Comparator<DelugeTorrent> {
        return when (this) {
            NAME -> By.name.compareBy(this)
            STATE -> state.compareBy(this)
            SIZE -> size.compareBy(this)
            PROGRESS -> progress.compareBy(this)
            DOWNLOADED -> downloaded.compareBy(this)
            RATIO -> ratio.compareBy(this)
            UPLOADED -> uploaded.compareBy(this)
            ETA -> eta.compareBy(this)
            DATE -> date.compareBy(this)
            DOWNLOAD_SPEED -> downloadSpeed.compareBy(this)
            UPLOAD_SPEED -> uploadSpeed.compareBy(this)
        }
    }

    /**
     * The [DelugeTorrent] is a parsed torrent returned from deluge, so
     * things like [DelugeTorrent.size] is already stored in `125 GiB` string form, which
     * causes sorting issues. This interface is a means to provide custom sorting for each
     * field, if necessary.
     */
    fun interface ByComparator<V : Comparable<V>, C : Comparable<C>> {
        fun comparable(value: V): Comparable<C>
    }

    companion object {

        val name = by<String>()

        val state = by<String>()

        val size = bySize()

        val progress = by<Short>()

        val downloaded = bySize()

        val ratio = bySize()

        val uploaded = bySize()

        val eta = by<String>() // todo

        val date = ByComparator<String, LocalDate> { LocalDate.parse(it, DelugeTorrentConverter.dateTimeFormatter) }

        val downloadSpeed = bySize()

        val uploadSpeed = bySize()

        private fun <V: Comparable<V>> by(): ByComparator<V, V> {
            return ByComparator { it }
        }

        private fun bySize(): ByComparator<String, Double> {
            return ByComparator {
                return@ByComparator if (it.isEmpty()) {
                     0.0
                } else {
                    val size = it.substringBefore(" ").toDouble()
                    val multiplier = multiplier(it.substringAfter(" "))
                    size * multiplier
                }
            }
        }
    }
}

/**
 * @return a [DelugeTorrent] comparator based on the field corresponding to the provided [By]
 * @param V the `in` type, or the type the [DelugeTorrent] field
 * @param R the reified [V] type for convenience
 * @param C the resulting comparable type
 * @see DelugeTorrent.getterBy
 */
inline fun <V : Comparable<V>, C : Comparable<C>, reified R : V> ByComparator<V, C>.compareBy(by: By): Comparator<DelugeTorrent>  {
    return compareBy { comparable(it.getterBy<R>(by).call(it)) }
}

private fun multiplier(it: String):Double {
    return when {
        it.contains("KiB") -> 1024.0
        it.contains("MiB") -> 1024.0 * 1024.0
        it.contains("GiB") -> 1024.0 * 1024.0 * 1024.0
        else -> 1.0
    }
}