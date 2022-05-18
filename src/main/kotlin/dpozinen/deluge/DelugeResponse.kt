package dpozinen.deluge

data class DelugeResponse(
    val result: Any?,
    val id: Long?,
    val error: Map<String, Any>?
) {

    fun errMsg(): String = (error ?: mapOf("message" to "no errors"))["message"] as String

    @Suppress("UNCHECKED_CAST")
    fun torrents(): Map<String, Map<String, *>> {
        result ?: throw IllegalArgumentException("no result")

        (result as Map<String, *>)["torrents"] ?: throw IllegalArgumentException("no torrents")

        return result["torrents"] as Map<String, Map<String, *>>
    }
}