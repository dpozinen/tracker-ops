package music

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import dpozinen.music.sockseek.SockseekClient
import dpozinen.music.sockseek.SockseekJobRequest
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.assertj.core.api.Assertions.assertThat
import org.springframework.cloud.openfeign.support.SpringMvcContract
import kotlin.test.Test

@WireMockTest(httpPort = 9996)
class SockseekClientTest {

	private val mapper = jacksonObjectMapper()

	private val client: SockseekClient = Feign.builder()
		.contract(SpringMvcContract())
		.encoder(JacksonEncoder(mapper))
		.decoder(JacksonDecoder(mapper))
		.target(SockseekClient::class.java, "http://localhost:9996")

	@Test
	fun `submitJob posts input url and returns job response`() {
		stubFor(
			post(urlEqualTo("/api/jobs/extract"))
				.withRequestBody(matchingJsonPath("$.input", equalTo("https://open.spotify.com/playlist/abc")))
				.withRequestBody(matchingJsonPath("$.autoStartExtractedResult", equalTo("true")))
				.willReturn(okJson("""{"jobId":"abc123","lifecycleState":"Queued"}"""))
		)

		val response = client.submitJob(SockseekJobRequest("https://open.spotify.com/playlist/abc"))

		assertThat(response).isEqualTo(
			dpozinen.music.sockseek.SockseekJobResponse(jobId = "abc123", lifecycleState = "Queued")
		)
	}

	@Test
	fun `getJob returns detail with summary and payload`() {
		stubFor(get(urlEqualTo("/api/jobs/song-1"))
			.willReturn(okJson(stringResource("sockseek/job-detail.json"))))

		val detail = client.getJob("song-1")

		assertThat(detail).isEqualTo(
			dpozinen.music.sockseek.SockseekJobDetail(
				summary = dpozinen.music.sockseek.SockseekJobSummary(
					jobId = "song-1", workflowId = "wf-1", kind = "song",
					lifecycleState = "Terminal", terminalOutcome = "Succeeded", parentJobId = "jl-1",
				),
				payload = dpozinen.music.sockseek.SockseekPayload(
					downloadPath = "/music/6YNTHMANE/FINA LANA (SLOWED)/01. FINA LANA (SLOWED).flac",
					downloadSource = "Soulseek",
				),
			)
		)
	}

	@Test
	fun `getJobs returns flat summaries for a workflow`() {
		stubFor(get(urlPathEqualTo("/api/jobs"))
			.withQueryParam("workflowId", equalTo("wf-1"))
			.withQueryParam("includeAll", equalTo("true"))
			.willReturn(okJson(stringResource("sockseek/jobs-by-workflow.json"))))

		val jobs = client.getJobs("wf-1")

		assertThat(jobs.map { it.jobId to it.kind }).isEqualTo(listOf(
			"ex-1" to "extract", "jl-1" to "job-list",
			"song-1" to "song", "song-2" to "song", "song-3" to "song",
		))
	}

	private fun stringResource(path: String) =
		SockseekClientTest::class.java.classLoader.getResource(path)!!.readText()
}
