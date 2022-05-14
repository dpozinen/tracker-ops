package dpozinen.deluge

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit.HOURS
import kotlin.math.floor
import kotlin.math.round
import kotlin.math.roundToInt

class DelugeTorrentConverter(
    private val torrent: Map.Entry<String, Map<String, *>>,
    private val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
        .withZone(ZoneId.systemDefault()),
) {


    fun convert(): DelugeTorrent {
        val id = torrent.key
        val fields = torrent.value

        val name = field<String>(fields, "name")
        val state = field<String>(fields, "state")
        val progress = field<Short>(fields, "progress")
        val uploaded = field<Double, String>(fields, "total_uploaded") { bytesToSize(it) }
        val downloaded = field<Double, String>(fields, "total_done") { bytesToSize(it) }
        val size = field<Double, String>(fields, "total_wanted") { bytesToSize(it) }
        val ratio = field<Double, String>(fields, "ratio") { return@field if (it < 0) "-" else it.round(2).toString() }

        val eta = field<Double, String>(fields, "eta") { eta(it) }
        val date = field<Long, String>(fields, "time_added") { date(it) }
        val downSpeed = field<Double, String>(fields, "download_payload_rate") { bytesToSpeed(it) }
        val upSpeed = field<Double, String>(fields, "upload_payload_rate") { bytesToSpeed(it) }

        return DelugeTorrent(
            id, name, state, progress, size, downloaded, ratio, uploaded, downSpeed, eta, upSpeed, date
        )
    }

    private fun date(timestamp: Long): String {
        val gmtPlusThree = Instant.ofEpochSecond(timestamp).plus(3, HOURS) // hours
        return dateTimeFormatter.format(gmtPlusThree)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> field(map: Map<String, *>, key: String): T = map[key] as T

    @Suppress("UNCHECKED_CAST")
    fun <T, R> field(map: Map<String, *>, key: String, covert: (T) -> R): R = covert.invoke(map[key] as T)

    private fun bytesToSize(bytes: Double): String {
        return when {
            bytes <= 0 -> ""
            bytes / 1024 < 1024 -> "${(bytes / 1024).round(2)} KiB"
            bytes / 1024 / 1024 < 1024 -> "${(bytes / 1024 / 1024).round(2)} MiB"
            else -> "${(bytes / 1024 / 1024 / 1024).round(2)} GiB"
        }
    }

    private fun bytesToSpeed(bytes: Double): String {
        return when (val size = bytesToSize(bytes)) {
            "" -> size
            else -> "$size/s"
        }
    }

    private fun eta(eta: Double): String {
        val time = eta.round(0)

        return when {
            eta <= 0 -> ""
            time < 60 -> "$time s"
            time / 60 < 60 -> timePrettyString(time / 60, "m", "s")
            time / 60 / 60 < 24 -> timePrettyString(time / 60 / 60, "h", "m")
            else -> timePrettyString(time / 60 / 60 / 24, "d", "h")
        }
    }

    private fun timePrettyString(time: Double, aName: String, bName: String): String {
        val tmpA = floor(time)
        val tmpB = (60 * (time - tmpA)).roundToInt()

        return if (tmpB > 0)
            "$tmpA $aName $tmpB $bName"
        else
            "$tmpA $aName"
    }

}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}