package dpozinen.deluge.rest

import dpozinen.deluge.rest.dto.HostsResponse
import feign.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@FeignClient(
    name = "deluge-auth",
    url = "\${tracker-ops.deluge.host}:\${tracker-ops.deluge.port}",
    path = "/json"
)
interface DelugeAuthClient {

    @PostMapping
    @Headers("Content-Type: application/json")
    fun hosts(@RequestBody body: DelugeRequest): HostsResponse

    @PostMapping
    @Headers("Content-Type: application/json")
    fun connect(@RequestBody body: DelugeRequest)

    @PostMapping
    @Headers("Content-Type: application/json")
    fun login(@RequestBody body: DelugeRequest): Response

}