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

	private fun summary(id: String, kind: String, outcome: String) =
		SockseekJobSummary(id, "wf-1", kind, "Terminal", outcome, "jl-1")

	@Test
	fun `keeps soulseek-downloaded songs, drops failed and fallback`() {
		val harvester = SockseekHarvester(sockseek)
		every { sockseek.getJob("ex-1") } returns
			SockseekJobDetail(summary("ex-1", "extract", "Succeeded"), null)
		every { sockseek.getJobs("wf-1", includeAll = true) } returns listOf(
			summary("ex-1", "extract", "Succeeded"),
			summary("jl-1", "job-list", "Succeeded"),
			summary("song-1", "song", "Succeeded"),   // soulseek → keep
			summary("song-2", "song", "Succeeded"),   // fallback → drop
			summary("song-3", "song", "Failed"),      // failed → drop
		)
		every { sockseek.getJob("song-1") } returns SockseekJobDetail(
			summary("song-1", "song", "Succeeded"),
			SockseekPayload("/music/A/Album/01. a.flac", "Soulseek"))
		every { sockseek.getJob("song-2") } returns SockseekJobDetail(
			summary("song-2", "song", "Succeeded"),
			SockseekPayload("/music/.opus", "Fallback"))

		val paths = harvester.resolve("ex-1")

		assertThat(paths).isEqualTo(listOf("/music/A/Album/01. a.flac"))
	}
}
