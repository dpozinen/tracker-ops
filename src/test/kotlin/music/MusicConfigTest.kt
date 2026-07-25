package music

import dpozinen.music.MusicConfig
import org.assertj.core.api.Assertions.assertThat
import java.time.Duration
import kotlin.test.Test

class MusicConfigTest {

	@Test
	fun `plex config defaults`() {
		assertThat(MusicConfig.PlexConfig()).isEqualTo(
			MusicConfig.PlexConfig(
				enabled = true,
				libraryId = 11,
				sockseekPathPrefix = "/music/",
				plexPathPrefix = "/media/chute/Song/",
				wait = MusicConfig.PlexConfig.Wait(interval = Duration.ofSeconds(30), maxAttempts = 240),
			)
		)
	}
}
