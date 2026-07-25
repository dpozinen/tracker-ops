package dpozinen.music

import dpozinen.health.rest.Telegram
import dpozinen.music.plex.PlaylistResult
import dpozinen.music.plex.PlexPlaylistClient
import dpozinen.music.plex.PlexPlaylistSyncer
import dpozinen.music.sockseek.SockseekClient
import dpozinen.music.sockseek.SockseekHarvester
import dpozinen.music.sockseek.SockseekJobRequest
import dpozinen.music.sockseek.SockseekJobResponse
import dpozinen.music.sockseek.SockseekStates
import dpozinen.music.spotify.SpotifyClient
import dpozinen.music.spotify.SpotifyPlaylist
import feign.FeignException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging.logger
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.annotation.Profile
import org.springframework.context.event.EventListener
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.stereotype.Service

@Service
class MusicSyncJob(
	private val spotify: SpotifyClient,
	private val sockseek: SockseekClient,
	private val telegram: Telegram,
	private val config: MusicConfig,
	private val harvester: SockseekHarvester,
	private val syncer: PlexPlaylistSyncer,
	private val plex: PlexPlaylistClient,
) {
	private val log = logger {}

	@Scheduled(cron = "\${zoe.music.cron}")
	fun sync() {
		log.info { "Starting music sync" }
		val myId = spotify.getMe().id
		val playlists = fetchAllPlaylists(myId)

		val results = mutableMapOf<String, String>()
		val jobs = LinkedHashMap<String, String>()   // playlistName -> extract jobId

		for (playlist in playlists) {
			val url = "https://open.spotify.com/playlist/${playlist.id}"
			val response = submit(playlist.name, url)
			results[playlist.name] = response.lifecycleState
			response.jobId?.let { jobs[playlist.name] = it }
		}
		results["Liked Songs"] = submit("Liked Songs", "spotify:liked").lifecycleState
		results["Saved Albums"] = submit("Saved Albums", "spotify:albums").lifecycleState

		telegram.sendMessage(telegram.chatId, buildSummary(results))

		if (config.plex.enabled) syncToPlex(jobs)
		log.info { "Music sync complete" }
	}

	private fun syncToPlex(jobs: Map<String, String>) {
		if (jobs.isEmpty()) return
		awaitDownloads(jobs.values)
		awaitScan()
		val resolved = jobs.mapValues { (_, jobId) -> harvester.resolve(jobId) }
		telegram.sendMessage(telegram.chatId, buildPlexSummary(syncer.sync(resolved)))
	}

	private fun awaitDownloads(jobIds: Collection<String>) = runBlocking {
		val workflows = jobIds.map { sockseek.getJob(it).summary.workflowId }.toSet()
		repeat(config.plex.wait.maxAttempts) {
			val done = workflows.all { wf ->
				sockseek.getJobs(wf, includeAll = true).all { it.lifecycleState == SockseekStates.TERMINAL }
			}
			if (done) return@runBlocking
			delay(config.plex.wait.interval.toMillis())
		}
		log.warn { "Downloads still running after cap; syncing what resolved" }
	}

	private fun awaitScan() = runBlocking {
		plex.scan(config.plex.libraryId)
		repeat(config.plex.wait.maxAttempts) {
			val refreshing = plex.sections().container.directory
				.firstOrNull { it.key == config.plex.libraryId.toString() }?.refreshing ?: false
			if (!refreshing) return@runBlocking
			delay(config.plex.wait.interval.toMillis())
		}
		log.warn { "Plex scan still running after cap; proceeding" }
	}

	private fun submit(name: String, url: String): SockseekJobResponse {
		return try {
			val response = sockseek.submitJob(SockseekJobRequest(url))
			log.info { "Submitted $name → job ${response.jobId} (${response.lifecycleState})" }
			response
		} catch (e: FeignException.FeignClientException) {
			val msg = "❌ Music sync failed: $name — ${e.status()} ${e.message}"
			log.error { msg }
			telegram.sendMessage(telegram.chatId, msg)
			SockseekJobResponse(jobId = null, lifecycleState = "failed (${e.status()})")
		} catch (e: Exception) {
			val msg = "❌ Music sync failed: $name — ${e.message}"
			log.error(e) { msg }
			telegram.sendMessage(telegram.chatId, msg)
			SockseekJobResponse(jobId = null, lifecycleState = "failed")
		}
	}

	private fun buildPlexSummary(results: List<PlaylistResult>): String {
		val lines = results.joinToString("\n") {
			"✓ ${it.name} — +${it.added}/-${it.removed}" +
				if (it.unresolved > 0) " (${it.unresolved} not in plex)" else ""
		}
		return "🎧 Plex playlists synced\n$lines"
	}

	private fun fetchAllPlaylists(myId: String): List<SpotifyPlaylist> {
		val playlists = mutableListOf<SpotifyPlaylist>()
		var offset = 0
		do {
			val page = spotify.getPlaylists(50, offset)
			for (pl in page.items) {
				if (pl.owner.id == myId || pl.name in config.spotify.additionalPlaylists) {
					if (!pl.collaborative || config.spotify.includeCollaborative) {
						playlists.add(pl)
						log.info { "Including playlist: ${pl.name}" }
					}
				} else {
					log.info { "Skipping playlist: ${pl.name} (owner: ${pl.owner.id})" }
				}
			}
			offset += page.items.size
		} while (page.next != null)
		return playlists
	}

	@Component
	@Profile("!test")
	private class MusicSyncStartup(private val job: MusicSyncJob) {
		@EventListener(ApplicationReadyEvent::class)
		fun onReady() = job.sync()
	}
}

private fun buildSummary(results: Map<String, String>): String {
	val lines = results.entries.joinToString("\n") { (name, status) ->
		if (status.startsWith("failed")) "❌ $name — $status"
		else "✓ $name — $status"
	}
	return "🎵 Music sync complete\n$lines"
}
