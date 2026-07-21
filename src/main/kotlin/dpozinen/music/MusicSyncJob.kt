package dpozinen.music

import dpozinen.health.rest.Telegram
import dpozinen.music.sockseek.SockseekClient
import dpozinen.music.sockseek.SockseekJobRequest
import dpozinen.music.spotify.SpotifyClient
import dpozinen.music.spotify.SpotifyPlaylist
import feign.FeignException
import mu.KotlinLogging.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class MusicSyncJob(
	private val spotify: SpotifyClient,
	private val sockseek: SockseekClient,
	private val telegram: Telegram,
	private val config: MusicConfig,
) {
	private val log = logger {}

	@Scheduled(cron = "\${zoe.music.cron}")
	fun sync() {
		log.info { "Starting music sync" }
		val myId = spotify.getMe().id
		val playlists = fetchAllPlaylists(myId)

		val results = mutableMapOf<String, String>()

		for (playlist in playlists) {
			val url = "https://open.spotify.com/playlist/${playlist.id}"
			results[playlist.name] = submit(playlist.name, url)
		}
		results["Liked Songs"] = submit("Liked Songs", "spotify:liked")
		results["Saved Albums"] = submit("Saved Albums", "spotify:albums")

		telegram.sendMessage(telegram.chatId, buildSummary(results))
		log.info { "Music sync complete" }
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
			offset += 50
		} while (page.next != null)
		return playlists
	}

	private fun submit(name: String, url: String): String {
		return try {
			val response = sockseek.submitJob(SockseekJobRequest(url))
			log.info { "Submitted $name → job ${response.id} (${response.status})" }
			response.status
		} catch (e: FeignException.FeignClientException) {
			// 4xx — no retry, alert immediately
			val msg = "❌ Music sync failed: $name — ${e.status()} ${e.message}"
			log.error { msg }
			telegram.sendMessage(telegram.chatId, msg)
			"failed (${e.status()})"
		} catch (e: Exception) {
			val msg = "❌ Music sync failed: $name — ${e.message}"
			log.error(e) { msg }
			telegram.sendMessage(telegram.chatId, msg)
			"failed"
		}
	}

	private fun buildSummary(results: Map<String, String>): String {
		val lines = results.entries.joinToString("\n") { (name, status) ->
			if (status.startsWith("failed")) "❌ $name — $status"
			else "✓ $name — $status"
		}
		return "🎵 Music sync complete\n$lines"
	}
}
