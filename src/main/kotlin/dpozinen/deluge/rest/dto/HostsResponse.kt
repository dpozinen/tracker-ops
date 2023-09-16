package dpozinen.deluge.rest.dto

import com.fasterxml.jackson.annotation.JsonIgnoreProperties


@JsonIgnoreProperties(ignoreUnknown = true)
data class HostsResponse(val result: List<List<String>>) {
    fun id() = result[0][0]
}

