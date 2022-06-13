package dpozinen.deluge.domain

import java.time.LocalDateTime

data class DataPoint(
    val id: Long,
    val torrentId: String,
    var time: LocalDateTime,
    val upSpeed: Long,
    val upSpeedBytes: String,
    val downSpeed: Long,
    val downSpeedBytes: String,
    val uploaded: Long,
    val uploadedBytes: String,
    val downloaded: Long,
    val downloadedBytes: String,
) {
    fun emptyCopy(time: LocalDateTime) = this.copy(id = -1, time = time, downSpeed = 0, downSpeedBytes = "", upSpeed = 0, upSpeedBytes = "")

    fun isEmptyCopy() = this.id == -1L
}