package dpozinen.deluge

data class DelugeResponse(
    val result: Boolean?,
    val id: Long?,
    val error: Map<String, Any>?
) {
    fun errMsg(): String = (error ?: mapOf(Pair("message", "no errors")))["message"] as String
}