package dpozinen.music.sockseek

import mu.KotlinLogging.logger
import org.springframework.stereotype.Service

@Service
class SockseekHarvester(private val sockseek: SockseekClient) {
	private val log = logger {}

	fun resolve(extractJobId: String): List<String> {
		val workflowId = sockseek.getJob(extractJobId).summary.workflowId
		val songs = sockseek.getJobs(workflowId, includeAll = true)
			.filter { it.kind == SockseekStates.SONG && it.terminalOutcome == SockseekStates.SUCCEEDED }
		val paths = songs
			.mapNotNull { sockseek.getJob(it.jobId).payload }
			.filter { it.downloadSource == SockseekStates.SOULSEEK }
			.mapNotNull { it.downloadPath }
		log.info { "Workflow $workflowId (job $extractJobId): ${songs.size} succeeded songs, ${paths.size} soulseek downloads" }
		return paths
	}
}
