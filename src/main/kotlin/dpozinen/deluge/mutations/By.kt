package dpozinen.deluge.mutations

import dpozinen.deluge.DelugeTorrent
import dpozinen.deluge.mutations.By.ByComparable
import dpozinen.deluge.mutations.By.ByPredicate
import dpozinen.deluge.rest.DelugeTorrentConverter
import dpozinen.deluge.rest.sizeToBytes
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

        fun or(other: ByPredicate) = ByPredicate { this.test(it) || other.test(it) }
    }

    companion object {

        val name = by()
        val state = by()
        val size = bySize()
        val progress = by()
        val downloaded = bySize()
        val ratio = bySize()
        val uploaded = bySize()
        val downloadSpeed = bySize()
        val uploadSpeed = bySize()

        @Suppress("EXPERIMENTAL_IS_NOT_ENABLED")
        @OptIn(ExperimentalTime::class)
        val eta = ByComparable { if (it.isEmpty()) 0 else kotlin.time.Duration.parse(it).toLong(MINUTES) }

        val date = ByComparable { LocalDate.parse(it, DelugeTorrentConverter.dateTimeFormatter) }

        private fun by() = ByComparable { it }

        fun bySize() =
            ByComparable {
                if (it.isEmpty()) {
                    0.0
                } else {
                    val size = it.substringBefore(" ").toDouble()
                    val multiplier = sizeToBytes(it.substringAfter(" "))
                    size * multiplier
                }
            }
    }
}
