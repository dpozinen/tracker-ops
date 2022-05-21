package dpozinen.deluge.mutations

import dpozinen.deluge.DelugeTorrent
import dpozinen.deluge.DelugeTorrentConverter
import dpozinen.deluge.mutations.By.ByComparator
import dpozinen.deluge.sizeToBytes
import java.time.LocalDate
import java.util.Comparator
import kotlin.time.DurationUnit.MINUTES
import kotlin.time.ExperimentalTime

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
            NAME -> comparedBy(By.name)
            STATE -> comparedBy(state)
            SIZE -> comparedBy(size)
            PROGRESS -> comparedBy(progress)
            DOWNLOADED -> comparedBy(downloaded)
            RATIO -> comparedBy(ratio)
            UPLOADED -> comparedBy(uploaded)
            ETA -> comparedBy(eta)
            DATE -> comparedBy(date)
            DOWNLOAD_SPEED -> comparedBy(downloadSpeed)
            UPLOAD_SPEED -> comparedBy(uploadSpeed)
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

        @OptIn(ExperimentalTime::class)
        val eta = ByComparator<String, Long> {
            return@ByComparator if (it.isEmpty()) 0 else kotlin.time.Duration.parse(it).toLong(MINUTES)
        }

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
                    val multiplier = sizeToBytes(it.substringAfter(" "))
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
inline fun <V : Comparable<V>, C : Comparable<C>, reified R : V> By.comparedBy(comparator: ByComparator<V, C>): Comparator<DelugeTorrent>  {
    return compareBy<DelugeTorrent> { comparator.comparable(it.getterBy<R>(this).call(it)) }
}