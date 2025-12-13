package dpozinen.health.rest

import com.fasterxml.jackson.annotation.JsonProperty

data class GetUpdatesResponse(val result: List<Result>) {

    data class Result(
        @JsonProperty("update_id")
        val updateId: Long,
        val message: Message
    )

}
