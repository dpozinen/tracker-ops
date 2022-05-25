package dpozinen.deluge

@Suppress("UNCHECKED_CAST")
data class DelugeResponse(
    val result: Any?,
    val id: Long?,
    val error: Map<String, Any>?
) {

    fun errMsg(): String = (error ?: mapOf("message" to "no errors"))["message"] as String

    fun torrents(): Map<String, Map<String, *>> {
        result ?: throw IllegalArgumentException("no result")

        (result as Map<String, *>)["torrents"] ?: throw IllegalArgumentException("no torrents")

        return result["torrents"] as Map<String, Map<String, *>>
    }

    fun isConnected(): Boolean {
        result ?: throw IllegalArgumentException("no result")
        val connected = (result as Map<String, *>)["connected"] as Boolean?

        return connected ?: false
    }

    fun hosts(): List<DelugeHost> {
        result ?: throw IllegalArgumentException("no result")

        return (result as List<List<*>>)
            .map { DelugeHost(it[0] as String, it[1] as String, it[2] as Int, it[3] as String) }
    }

    data class DelugeHost(val id: String, val host: String, val port: Int, val client: String)
}