package dpozinen.music.sockseek

import mu.KotlinLogging.logger
import org.springframework.stereotype.Service

@Service
class SockseekHarvester(private val sockseek: SockseekClient) {
	private val log = logger {}

	fun resolve(extractJobId: String): List<String> {
		val workflowId = sockseek.getJob(extractJobId).summary.workflowId
		val songs = sockseek.getJobs(workflowId, includeAll = true)
			.filter { it.kind == SockseekStates.SONG }
			.filter { it.terminalOutcome == SockseekStates.SUCCEEDED || it.skipReason == SockseekStates.ALREADY_EXISTS }
		val paths = songs
			.mapNotNull { sockseek.getJob(it.jobId).payload }
			.filter { it.downloadSource == SockseekStates.SOULSEEK || it.skipReason == SockseekStates.ALREADY_EXISTS }
			.mapNotNull { it.downloadPath }
		log.info { "Workflow $workflowId (job $extractJobId): ${songs.size} succeeded/existing songs, ${paths.size} resolved paths" }
		return paths
	}
}
