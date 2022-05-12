import dpozinen.deluge.*
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.HttpHeaders
import org.springframework.http.ResponseEntity
import java.net.HttpCookie
import kotlin.test.Test


class DelugeTest {

    @Test
    fun `should convert`() {
        val torrent = DelugeTorrentConverter(Data.delugeTorrentResponse).convert()

        assertThat(torrent)
            .isEqualTo(
                DelugeTorrent(
                    id = "ee21ac410a4df9d2a09a97a6890fc74c0d143a0b",
                    name = "Rick and Morty Season 1  [2160p AI x265 FS100 Joy]",
                    state = "Seeding",
                    progress = 100,
                    size = "8.11 GiB",
                    ratio = "67.9",
                    uploaded = "550.96 GiB",
                    downloaded = "8.11 GiB",
                    eta = "-",
                    downloadSpeed = "",
                    uploadSpeed = "",
                    date = "28.06.2021"
                )
            )
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
        every { delugeClient.torrents(DelugeParams.torrents(), Data.sessioIdHttpCookie) } returns response

        return delugeClient
    }

    private fun httpHeaders(): HttpHeaders {
        val httpHeaders = HttpHeaders()
        httpHeaders["Set-Cookie"] = Data.sessionIdCookie
        return httpHeaders
    }


}