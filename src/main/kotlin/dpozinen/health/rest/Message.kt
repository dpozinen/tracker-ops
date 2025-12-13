package dpozinen.health.rest


data class Message(
    val id: Long,
    val from: FromResponse,
    val chat: ChatResponse,
    val text: String
)

data class ChatResponse(
    val id: Long
)

data class FromResponse(
    val id: Long
)