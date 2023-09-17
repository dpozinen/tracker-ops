package dpozinen.deluge.rest.clients

import dpozinen.deluge.rest.DelugeRequest
import feign.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody


@FeignClient(
    name = "deluge-auth",
    url = "\${tracker-ops.deluge.url}",
    path = "/json"
)
interface DelugeAuthClient {

    @PostMapping
    @Headers("Content-Type: application/json")
    fun login(@RequestBody body: DelugeRequest = DelugeRequest.login()): Response

}