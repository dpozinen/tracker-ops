package dpozinen.music.sockseek

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import feign.Retryer
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
	name = "sockseek",
	url = "\${zoe.music.sockseek.url}",
	configuration = [SockseekClient.Config::class],
)
interface SockseekClient {

	@PostMapping("/api/jobs/extract")
	fun submitJob(@RequestBody request: SockseekJobRequest): SockseekJobResponse

	@GetMapping("/api/jobs/{jobId}")
	fun getJob(@PathVariable jobId: String): SockseekJobDetail

	@GetMapping("/api/jobs")
	fun getJobs(
		@RequestParam workflowId: String,
		@RequestParam includeAll: Boolean = true,
	): List<SockseekJobSummary>

	open class Config {
		@Bean
		open fun retryer() = Retryer.Default()
	}
}

data class SockseekJobRequest(
	val input: String,
	val autoStartExtractedResult: Boolean = true,
)

data class SockseekJobResponse(
	val jobId: String,
	val lifecycleState: String,
)

data class SockseekJobDetail(
	val summary: SockseekJobSummary,
	val payload: SockseekPayload?,
)

data class SockseekJobSummary(
	val jobId: String,
	val workflowId: String,
	val kind: String,
	val lifecycleState: String,
	val terminalOutcome: String? = null,
	val parentJobId: String? = null,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class SockseekPayload(
	val downloadPath: String? = null,
	val downloadSource: String? = null,
)

object SockseekStates {
	const val TERMINAL = "Terminal"
}
