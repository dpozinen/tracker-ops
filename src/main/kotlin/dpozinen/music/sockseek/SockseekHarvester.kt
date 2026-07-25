package dpozinen.music.sockseek

import org.springframework.stereotype.Service

@Service
class SockseekHarvester(private val sockseek: SockseekClient) {

	fun resolve(extractJobId: String): List<String> {
		val workflowId = sockseek.getJob(extractJobId).summary.workflowId
		return sockseek.getJobs(workflowId, includeAll = true)
			.filter { it.kind == "song" && it.terminalOutcome == "Succeeded" }
			.mapNotNull { sockseek.getJob(it.jobId).payload }
			.filter { it.downloadSource == "Soulseek" }
			.mapNotNull { it.downloadPath }
	}
}
