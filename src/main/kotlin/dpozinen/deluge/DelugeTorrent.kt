package dpozinen.deluge

data class DelugeTorrent(
    val id: String,
    val name: String,
    val state: String,
    val progress: Short,
    val size: String,
    val downloaded: String,
    val ratio: Double,
    val uploaded: String,
    val downloadSpeed: String,
    val eta: String,
    val uploadSpeed: String,
    val date: String
)

