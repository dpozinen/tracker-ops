package dpozinen.deluge.domain

data class DelugeTorrent(
    val id: String,
    val name: String,
    val state: String,
    val progress: String,
    val size: String,
    val downloaded: String,
    val ratio: String,
    val uploaded: String,
    val downloadSpeed: String,
    val eta: String,
    val uploadSpeed: String,
    val date: String
) {

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as DelugeTorrent

        return id == other.id
    }

    override fun hashCode() = id.hashCode()

}

