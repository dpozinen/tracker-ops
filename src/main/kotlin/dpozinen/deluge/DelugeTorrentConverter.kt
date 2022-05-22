package dpozinen.deluge

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern
import kotlin.math.round
import kotlin.time.ExperimentalTime

class DelugeTorrentConverter(
    private val torrent: Map.Entry<String, Map<String, *>>,
) {

    fun convert(): DelugeTorrent {
        val id = torrent.key
        val fields = torrent.value

        val name = field<String>(fields, "name")
        val state = field<String>(fields, "state")
        val progress = field<Double, String>(fields, "progress") { roundDouble(it) }
        val uploaded = field<Double, String>(fields, "total_uploaded") { bytesToSize(it) }
        val downloaded = field<Double, String>(fields, "total_done") { bytesToSize(it) }
        val size = field<Double, String>(fields, "total_wanted") { bytesToSize(it) }
        val ratio = field<Double, String>(fields, "ratio") { ratio(it, fields) }

        val eta = field<Double, String>(fields, "eta") { eta(it) }
        val date = field<Long, String>(fields, "time_added") { date(it) }
        val downSpeed = field<Double, String>(fields, "download_payload_rate") { bytesToSpeed(it) }
        val upSpeed = field<Double, String>(fields, "upload_payload_rate") { bytesToSpeed(it) }

        return DelugeTorrent(
            id, name, state, progress, size, downloaded, ratio, uploaded, downSpeed, eta, upSpeed, date
        )
    }

    private fun ratio(it: Double, fields: Map<String, *>) =
        if (it == -1.0)
            roundDouble((fields["total_uploaded"] as Long / fields["total_wanted"] as Long).toDouble())
        else
            roundDouble(it)

    private fun roundDouble(it: Double) = if (it < 0) "-" else it.round(2).toString()

    private fun date(timestamp: Long): String {
        return dateTimeFormatter.format(Instant.ofEpochSecond(timestamp))
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

    @OptIn(ExperimentalTime::class)
    private fun eta(eta: Double): String {
        return kotlin.time.Duration.seconds(eta).toString()
    }

    companion object {
        val dateTimeFormatter: DateTimeFormatter = ofPattern("dd.MM.yyyy").withZone(ZoneId.of("Europe/Kiev"))
    }

}

fun Double.round(decimals: Int): Double {
    var multiplier = 1.0
    repeat(decimals) { multiplier *= 10 }
    return round(this * multiplier) / multiplier
}

fun sizeToBytes(size: String): Double {
    return when {
        size.contains("KiB") -> 1024.0
        size.contains("MiB") -> 1024.0 * 1024.0
        size.contains("GiB") -> 1024.0 * 1024.0 * 1024.0
        else -> 1.0
    }
}