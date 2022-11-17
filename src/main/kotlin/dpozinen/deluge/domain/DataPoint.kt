package dpozinen.deluge.domain

import java.time.Instant

data class DataPoint(
    val torrentId: String,
    val name: String,
    val size: Long,
    val dateAdded: Instant,

    val upSpeed: Long,
    val downSpeed: Long,
    val uploaded: Long,
    val downloaded: Long,
    val timestamp: Instant
)