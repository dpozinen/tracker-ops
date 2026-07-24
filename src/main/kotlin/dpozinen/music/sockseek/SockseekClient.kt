package dpozinen.music.sockseek

import feign.Retryer
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
	name = "sockseek",
	url = "\${zoe.music.sockseek.url}",
	configuration = [SockseekClient.Config::class],
)
interface SockseekClient {

	// ponytail: path assumed from plan; verify against /api/openapi.json on the running daemon
	@PostMapping("/api/download")
	fun submitJob(@RequestBody request: SockseekJobRequest): SockseekJobResponse

	open class Config {
		@Bean
		open fun retryer() = Retryer.Default()
	}
}

data class SockseekJobRequest(val url: String)

data class SockseekJobResponse(
	val id: String,
	val status: String,
)
