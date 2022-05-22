package dpozinen.deluge.mutations

import dpozinen.deluge.DelugeTorrent
import dpozinen.deluge.DelugeTorrentConverter
import dpozinen.deluge.mutations.By.ByComparable
import dpozinen.deluge.mutations.By.ByPredicate
import dpozinen.deluge.sizeToBytes
import java.time.LocalDate
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
    fun interface ByComparable<C : Comparable<C>> {
        fun comparable(value: String): C
    }

    fun interface ByPredicate {
        fun test(torrent: DelugeTorrent): Boolean

        fun or(other: ByPredicate) : ByPredicate {
            return ByPredicate { this.test(it) || other.test(it) }
        }
    }

    companion object {

        val name = by()

        val state = by()

        val size = bySize()

        val progress = by()

        val downloaded = bySize()

        val ratio = bySize()

        val uploaded = bySize()

        @OptIn(ExperimentalTime::class)
        val eta = ByComparable {
            return@ByComparable if (it.isEmpty()) 0 else kotlin.time.Duration.parse(it).toLong(MINUTES)
        }

        val date = ByComparable { LocalDate.parse(it, DelugeTorrentConverter.dateTimeFormatter) }

        val downloadSpeed = bySize()

        val uploadSpeed = bySize()

        private fun by(): ByComparable<String> {
            return ByComparable { it }
        }

        private fun bySize(): ByComparable<Double> {
            return ByComparable {
                return@ByComparable if (it.isEmpty()) {
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
 * @param C the resulting comparable type
 * @see DelugeTorrent.getterBy
 */
fun <C : Comparable<C>> By.comparedBy(comparator: ByComparable<C>): Comparator<DelugeTorrent>  {
    return compareBy { comparator.comparable(it.getterBy(this).call(it)) }
}

inline fun <C : Comparable<C>> By.predicateBy(
    comparator: ByComparable<C>,
    crossinline predicate: (C) -> Boolean
): ByPredicate  {
    return ByPredicate { predicate.invoke(comparator.comparable(it.getterBy(this).call(it))) }
}