package music

import dpozinen.music.MusicConfig
import dpozinen.music.plex.*
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.justRun
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class PlexPlaylistSyncerTest {

	@MockK lateinit var plex: PlexPlaylistClient
	private lateinit var syncer: PlexPlaylistSyncer

	@BeforeEach
	fun setup() {
		val config = MusicConfig(
			spotify = MusicConfig.SpotifyConfig(clientId = "id", clientSecret = "s", refreshToken = "r"),
		)
		syncer = PlexPlaylistSyncer(plex, config)
		every { plex.identity() } returns PlexIdentity(PlexIdentity.Id("machine-abc"))
		every { plex.tracks(11, 0, 500, 10) } returns PlexTracks(PlexTracks.Container(
			totalSize = 2, size = 2, metadata = listOf(
				PlexTrack("101", listOf(PlexMedia(listOf(PlexPart("/media/chute/Song/A/Album/01. a.flac"))))),
				PlexTrack("102", listOf(PlexMedia(listOf(PlexPart("/media/chute/Song/B/Album/02. b.flac"))))),
			),
		))
		every { plex.playlists() } returns PlexPlaylists(PlexPlaylists.Container(listOf(
			PlexPlaylist("900", "My Mix"),
		)))
		every { plex.playlistItems("900") } returns PlexPlaylistItems(PlexPlaylistItems.Container(listOf(
			PlexPlaylistItem("101", 5001),   // stays
			PlexPlaylistItem("199", 5002),   // removed (not desired)
		)))
		justRun { plex.addItems(any(), any()) }
		justRun { plex.removeItem(any(), any()) }
	}

	@Test
	fun `adds new tracks, removes stale, counts unresolved`() {
		val result = syncer.sync(mapOf("My Mix" to listOf(
			"/music/A/Album/01. a.flac",   // → 101, present
			"/music/B/Album/02. b.flac",   // → 102, add
			"/music/C/Album/03. c.flac",   // not in Plex → unresolved
		)))

		val uri = slot<String>()
		verify { plex.addItems("900", capture(uri)) }
		assertThat(uri.captured)
			.isEqualTo("server://machine-abc/com.plexapp.plugins.library/library/metadata/102")
		verify { plex.removeItem("900", 5002) }
		verify(exactly = 0) { plex.createPlaylist(any(), any(), any(), any()) }
		assertThat(result).isEqualTo(listOf(PlaylistResult("My Mix", added = 1, removed = 1, unresolved = 1)))
	}

	@Test
	fun `creates playlist when none exists`() {
		every { plex.playlists() } returns PlexPlaylists(PlexPlaylists.Container(emptyList()))
		every { plex.createPlaylist(any(), any(), any(), any()) } returns
			PlexPlaylists(PlexPlaylists.Container(listOf(PlexPlaylist("901", "Fresh"))))
		every { plex.playlistItems("901") } returns PlexPlaylistItems(PlexPlaylistItems.Container(emptyList()))

		val result = syncer.sync(mapOf("Fresh" to listOf("/music/A/Album/01. a.flac")))

		verify {
			plex.createPlaylist("Fresh",
				"server://machine-abc/com.plexapp.plugins.library/library/metadata/101", "audio", 0)
		}
		assertThat(result).isEqualTo(listOf(PlaylistResult("Fresh", added = 1, removed = 0, unresolved = 0)))
	}

	@Test
	fun `one playlist failure is isolated and marked failed, others still sync`() {
		every { plex.playlists() } returns PlexPlaylists(PlexPlaylists.Container(listOf(
			PlexPlaylist("900", "Good"),
			PlexPlaylist("901", "Bad"),
		)))
		every { plex.playlistItems("900") } returns PlexPlaylistItems(PlexPlaylistItems.Container(emptyList()))
		every { plex.playlistItems("901") } throws RuntimeException("plex down")

		val result = syncer.sync(mapOf(
			"Good" to listOf("/music/A/Album/01. a.flac"),
			"Bad" to listOf("/music/B/Album/02. b.flac"),
		))

		assertThat(result).isEqualTo(listOf(
			PlaylistResult("Good", added = 1, removed = 0, unresolved = 0),
			PlaylistResult("Bad", added = 0, removed = 0, unresolved = 1, failed = true),
		))
	}
}
