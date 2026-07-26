package music

import dpozinen.music.sockseek.*
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class SockseekHarvesterTest {

	@MockK lateinit var sockseek: SockseekClient

	private fun summary(id: String, kind: String, outcome: String? = null, skipReason: String? = null) =
		SockseekJobSummary(id, "wf-1", kind, "Terminal", outcome, skipReason, "jl-1")

	@Test
	fun `keeps soulseek and already-existing songs, drops failed and fallback`() {
		val harvester = SockseekHarvester(sockseek)
		every { sockseek.getJob("ex-1") } returns
			SockseekJobDetail(summary("ex-1", "extract", "Succeeded"), null)
		every { sockseek.getJobs("wf-1", includeAll = true) } returns listOf(
			summary("ex-1", "extract", "Succeeded"),
			summary("jl-1", "job-list", "Succeeded"),
			summary("song-1", "song", "Succeeded"),                    // soulseek → keep
			summary("song-2", "song", "Succeeded"),                    // fallback → drop
			summary("song-3", "song", "Failed"),                       // failed → drop
			summary("song-4", "song", "Skipped", "AlreadyExists"),     // already on disk → keep
		)
		every { sockseek.getJob("song-1") } returns SockseekJobDetail(
			summary("song-1", "song", "Succeeded"),
			SockseekPayload("/music/A/Album/01. a.flac", "Soulseek"))
		every { sockseek.getJob("song-2") } returns SockseekJobDetail(
			summary("song-2", "song", "Succeeded"),
			SockseekPayload("/music/.opus", "Fallback"))
		every { sockseek.getJob("song-4") } returns SockseekJobDetail(
			summary("song-4", "song", "Skipped", "AlreadyExists"),
			SockseekPayload("/music/B/Album/02. b.flac", null, "AlreadyExists"))

		val paths = harvester.resolve("ex-1")

		assertThat(paths).isEqualTo(listOf("/music/A/Album/01. a.flac", "/music/B/Album/02. b.flac"))
	}
}
