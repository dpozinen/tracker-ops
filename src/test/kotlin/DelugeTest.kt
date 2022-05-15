 import Data.Companion.httpHeaders
import dpozinen.deluge.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
 import org.springframework.http.ResponseEntity
import kotlin.test.Test


class DelugeTest {

    @Test
    fun `should convert`() {
        val torrent = DelugeTorrentConverter(Data.delugeTorrentResponse.entries.first()).convert()

        assertThat(torrent).isEqualTo(Data.delugeTorrent)
    }

    @Test
    fun `should throw if bad response`() {
        val delugeClient = mock(false)

        val service = DelugeService("", delugeClient)

        try {
            service.torrents()
        } catch (e: Exception) {
            assertThat(e)
                .hasMessage("no torrents")
                .isInstanceOf(IllegalArgumentException::class.java)
        }
    }

    @Test
    fun `should login once per active session`() {
        val delugeClient = mock()

        val service = DelugeService("", delugeClient)

        service.torrents()
        service.torrents()
        service.torrents()

        verify(exactly = 1) { delugeClient.login() }
    }

    private fun mock(mockTorrents: Boolean = true): DelugeClient {
        val delugeClient = mockk<DelugeClient>()
        val response = mockk<ResponseEntity<DelugeResponse>>()

        every { response.body } returns DelugeResponse(mapOf<String, Any>(), 123, mapOf())
        every { response.headers } returns httpHeaders()
        if (mockTorrents)
            every { response.body.torrents() } returns listOf()

        every { delugeClient.login() } returns response
        every { delugeClient.torrents(DelugeParams.torrents(setOf()), Data.sessionIdHttpCookie) } returns response

        return delugeClient
    }


}