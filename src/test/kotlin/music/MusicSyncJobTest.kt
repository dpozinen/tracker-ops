package music

import dpozinen.health.rest.Telegram
import dpozinen.music.MusicConfig
import dpozinen.music.MusicSyncJob
import dpozinen.music.sockseek.SockseekClient
import dpozinen.music.sockseek.SockseekJobRequest
import dpozinen.music.sockseek.SockseekJobResponse
import dpozinen.music.spotify.SpotifyClient
import dpozinen.music.spotify.SpotifyOwner
import dpozinen.music.spotify.SpotifyPagedPlaylists
import dpozinen.music.spotify.SpotifyPlaylist
import dpozinen.music.spotify.SpotifyUser
import feign.FeignException
import feign.Request
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class MusicSyncJobTest {

	@MockK lateinit var spotify: SpotifyClient
	@RelaxedMockK lateinit var sockseek: SockseekClient
	@RelaxedMockK lateinit var telegram: Telegram

	private lateinit var job: MusicSyncJob

	@BeforeEach
	fun setup() {
		val config = MusicConfig(
			spotify = MusicConfig.SpotifyConfig(
				clientId = "id",
				clientSecret = "secret",
				refreshToken = "refresh",
				additionalPlaylists = listOf("Extra Mix"),
			),
		)
		job = MusicSyncJob(spotify, sockseek, telegram, config)
		every { spotify.getMe() } returns SpotifyUser("me")
		every { sockseek.submitJob(any()) } returns SockseekJobResponse("id1", "queued")
	}

	@Test
	fun `submits owned playlists, liked songs, and albums`() {
		every { spotify.getPlaylists(50, 0) } returns SpotifyPagedPlaylists(
			items = listOf(
				SpotifyPlaylist("pl1", "My Mix", SpotifyOwner("me"), false),
				SpotifyPlaylist("pl2", "Their Mix", SpotifyOwner("other"), false),
			),
			next = null,
		)
		val captured = mutableListOf<SockseekJobRequest>()
		every { sockseek.submitJob(capture(captured)) } returns SockseekJobResponse("id1", "queued")

		job.sync()

		assertThat(captured.map { it.input }).isEqualTo(listOf(
			"https://open.spotify.com/playlist/pl1",
			"spotify:liked",
			"spotify:albums",
		))
	}

	@Test
	fun `includes additional playlists by name regardless of owner`() {
		every { spotify.getPlaylists(50, 0) } returns SpotifyPagedPlaylists(
			items = listOf(
				SpotifyPlaylist("pl1", "My Mix", SpotifyOwner("me"), false),
				SpotifyPlaylist("pl2", "Extra Mix", SpotifyOwner("other"), false),
			),
			next = null,
		)
		val captured = mutableListOf<SockseekJobRequest>()
		every { sockseek.submitJob(capture(captured)) } returns SockseekJobResponse("id1", "queued")

		job.sync()

		assertThat(captured.map { it.input }).isEqualTo(listOf(
			"https://open.spotify.com/playlist/pl1",
			"https://open.spotify.com/playlist/pl2",
			"spotify:liked",
			"spotify:albums",
		))
	}

	@Test
	fun `sends telegram alert on 4xx and continues`() {
		every { spotify.getPlaylists(50, 0) } returns SpotifyPagedPlaylists(
			items = listOf(
				SpotifyPlaylist("pl1", "My Mix", SpotifyOwner("me"), false),
				SpotifyPlaylist("pl2", "Bad Mix", SpotifyOwner("me"), false),
			),
			next = null,
		)
		val badUrl = "https://open.spotify.com/playlist/pl2"
		every { sockseek.submitJob(SockseekJobRequest(badUrl)) } throws FeignException.NotFound(
			"not found",
			Request.create(Request.HttpMethod.POST, "http://sockseek/api/download", emptyMap(), null, null, null),
			null,
			emptyMap(),
		)

		job.sync()

		verify { telegram.sendMessage(any(), match { it.contains("Bad Mix") && it.contains("❌") }) }
		verify { sockseek.submitJob(SockseekJobRequest("https://open.spotify.com/playlist/pl1")) }
	}

	@Test
	fun `sends summary telegram message after run`() {
		every { spotify.getPlaylists(50, 0) } returns SpotifyPagedPlaylists(
			items = listOf(SpotifyPlaylist("pl1", "My Mix", SpotifyOwner("me"), false)),
			next = null,
		)

		job.sync()

		verify { telegram.sendMessage(any(), match { it.contains("🎵") && it.contains("My Mix") }) }
	}

	@Test
	fun `paginates through multiple pages`() {
		every { spotify.getPlaylists(50, 0) } returns SpotifyPagedPlaylists(
			items = listOf(SpotifyPlaylist("pl1", "Page 1 Mix", SpotifyOwner("me"), false)),
			next = "https://api.spotify.com/v1/me/playlists?offset=50",
		)
		every { spotify.getPlaylists(50, 1) } returns SpotifyPagedPlaylists(
			items = listOf(SpotifyPlaylist("pl2", "Page 2 Mix", SpotifyOwner("me"), false)),
			next = null,
		)
		val captured = mutableListOf<SockseekJobRequest>()
		every { sockseek.submitJob(capture(captured)) } returns SockseekJobResponse("id1", "queued")

		job.sync()

		assertThat(captured.map { it.input }).isEqualTo(listOf(
			"https://open.spotify.com/playlist/pl1",
			"https://open.spotify.com/playlist/pl2",
			"spotify:liked",
			"spotify:albums",
		))
	}
}
