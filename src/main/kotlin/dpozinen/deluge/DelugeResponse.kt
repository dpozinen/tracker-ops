package dpozinen.deluge

data class DelugeResponse(
    val result: Map<String, *>?,
    val id: Long?,
    val error: Map<String, Any>?
) {

    fun errMsg(): String = (error ?: mapOf("message" to "no errors"))["message"] as String

    @Suppress("UNCHECKED_CAST")
    fun torrents(): List<DelugeTorrent> {
        result ?: throw IllegalArgumentException("no result")
        result["torrents"] ?: throw IllegalArgumentException("no torrents")

        val torrents = result["torrents"] as Map<String, Map<String, *>>

        return torrents.map { DelugeTorrentConverter(it) }.map { it.convert() }
    }
}