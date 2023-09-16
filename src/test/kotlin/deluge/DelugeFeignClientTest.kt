package deluge

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.marcinziolo.kotlin.wiremock.*
import dpozinen.App
import dpozinen.deluge.rest.DelugeFeignClient
import dpozinen.deluge.rest.DelugeRequest
import dpozinen.deluge.rest.DelugeResponse
import org.assertj.core.api.Assertions.assertThat
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import kotlin.test.Test

@SpringBootTest(
    classes = [App::class],
    webEnvironment = SpringBootTest.WebEnvironment.NONE
)
@ActiveProfiles("test")
class DelugeFeignClientTest {

    @Autowired
    lateinit var client: DelugeFeignClient

    @Test
    fun `should login`() {
        val response = client.send(DelugeRequest.torrents())

        assertThat(response.body)
            .isEqualTo(
                DelugeResponse(result = true, id = 8888, error = null)
            )
    }


}