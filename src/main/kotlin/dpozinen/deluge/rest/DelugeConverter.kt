package dpozinen.deluge.rest

import dpozinen.deluge.domain.DataPoint
import dpozinen.deluge.domain.DelugeTorrent
import dpozinen.deluge.mutations.By
import dpozinen.deluge.mutations.By.Companion.bySize
import dpozinen.deluge.rest.clients.TorrentsResult.TorrentResult
import org.springframework.stereotype.Component
import java.time.Instant
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatter.ofPattern
import kotlin.math.round
import kotlin.time.Duration.Companion.seconds

@Component
class DelugeConverter {

    fun convert(torrents: List<DelugeTorrent>) = torrents.map { convert(it) }

    fun convert(torrent: DelugeTorrent) = DataPoint(
        torrentId = torrent.id,
        name = torrent.name,
        size = bySize().comparable(torrent.size).toLong(),
        dateAdded = Instant.ofEpochSecond(By.date.comparable(torrent.date).toEpochSecond(LocalTime.NOON, ZoneOffset.UTC)),
        upSpeed = toBytes(torrent.uploadSpeed),
        downSpeed = toBytes(torrent.downloadSpeed),
        uploaded = toBytes(torrent.uploaded),
        downloaded = toBytes(torrent.downloaded),
        timestamp = Instant.now()
    )

    private fun toBytes(humanReadableForm: String) = bySize().comparable(humanReadableForm).toLong()

    fun convert(torrent: Map.Entry<String, Map<String, *>>): DelugeTorrent {
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

    fun convert(torrent: TorrentResult): DelugeTorrent {
        val id = torrent.id!!

        val name = torrent.name
        val state = torrent.state
        val progress = roundDouble(torrent.progress)
        val uploaded = bytesToSize(torrent.uploaded)
        val downloaded = bytesToSize(torrent.downloaded)
        val size = bytesToSize(torrent.size)
        val ratio = ratio(torrent.ratio, torrent.size, torrent.uploaded)

        val eta = eta(torrent.eta)
        val date = date(torrent.date)
        val downSpeed = bytesToSpeed(torrent.downloadSpeed)
        val upSpeed = bytesToSpeed(torrent.uploadSpeed)

        return DelugeTorrent(
            id, name, state, progress, size, downloaded, ratio, uploaded, downSpeed, eta, upSpeed, date
        )
    }

    private fun ratio(ratio: Double, size: Double, uploaded: Double): String {
        return if (ratio == -1.0) roundDouble(uploaded / size)
        else roundDouble(ratio)
    }

    private fun ratio(it: Double, fields: Map<String, *>) =
        if (it == -1.0)
            roundDouble(
                field<Double>(fields, "total_uploaded") / field<Double>(fields, "total_wanted")
            )
        else
            roundDouble(it)

    private fun roundDouble(it: Double) = if (it < 0) "-" else it.round(2).toString()

    private fun date(timestamp: Long) = dateTimeFormatter.format(Instant.ofEpochSecond(timestamp))

    @Suppress("UNCHECKED_CAST")
    fun <T> field(map: Map<String, *>, key: String): T = map[key] as T

    @Suppress("UNCHECKED_CAST")
    fun <T, R> field(map: Map<String, *>, key: String, covert: (T) -> R): R = covert(map[key] as T)

    private fun eta(eta: Double): String {
        return eta.seconds.toString()
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

fun sizeToBytes(size: String) =
    when {
        size.contains("KiB") -> 1024.0
        size.contains("MiB") -> 1024.0 * 1024.0
        size.contains("GiB") -> 1024.0 * 1024.0 * 1024.0
        size.contains("TiB") -> 1024.0 * 1024.0 * 1024.0 * 1024.0
        else -> 1.0
    }

fun bytesToSize(bytes: Double) =
    when {
        bytes <= 0 -> ""
        bytes / 1024 < 1024 -> "${(bytes / 1024).round(2)} KiB"
        bytes / 1024 / 1024 < 1024 -> "${(bytes / 1024 / 1024).round(2)} MiB"
        bytes / 1024 / 1024 / 1024 < 1024 -> "${(bytes / 1024 / 1024 / 1024).round(2)} GiB"
        else -> "${(bytes / 1024 / 1024 / 1024 / 1024).round(2)} TiB"
    }

fun bytesToSpeed(bytes: Double) =
    when (val size = bytesToSize(bytes)) {
        "" -> size
        else -> "$size/s"
    }