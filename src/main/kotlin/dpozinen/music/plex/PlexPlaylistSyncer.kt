package dpozinen.music.plex

import dpozinen.music.MusicConfig
import mu.KotlinLogging.logger
import org.springframework.stereotype.Service
import java.text.Normalizer

data class PlaylistResult(
	val name: String,
	val added: Int,
	val removed: Int,
	val unresolved: Int,
	val failed: Boolean = false,
)

@Service
class PlexPlaylistSyncer(
	private val plex: PlexPlaylistClient,
	config: MusicConfig,
) {
	private val log = logger {}
	private val plexConfig = config.plex
	private val pageSize = 500
	private val addChunk = 200

	fun sync(playlists: Map<String, List<String>>): List<PlaylistResult> {
		log.info { "Building Plex track index (section ${plexConfig.libraryId})" }
		val index = buildIndex()
		log.info { "Indexed ${index.size} Plex tracks" }
		val machineId = plex.identity().container.machineIdentifier
		val existing = plex.playlists().container.metadata.associate { it.title to it.ratingKey }
		log.info { "Found ${existing.size} existing Plex playlists; syncing ${playlists.size}" }
		return playlists.map { (name, paths) -> syncOne(name, paths, index, machineId, existing) }
	}

	private fun buildIndex(): Map<String, String> {
		val index = HashMap<String, String>()
		var start = 0
		while (true) {
			val page = plex.tracks(plexConfig.libraryId, start, pageSize).container
			page.metadata.forEach { track -> track.paths().forEach { index[nfc(it)] = track.ratingKey } }
			start += page.metadata.size
			if (page.metadata.isEmpty() || start >= page.totalSize) break
		}
		return index
	}

	private fun syncOne(
		name: String, paths: List<String>, index: Map<String, String>,
		machineId: String, existing: Map<String, String>,
	): PlaylistResult = runCatching {
		val resolved = LinkedHashSet<String>()
		var unresolved = 0
		paths.forEach { path ->
			val key = index[nfc(path.replaceFirst(plexConfig.sockseekPathPrefix, plexConfig.plexPathPrefix))]
			if (key != null) resolved.add(key) else unresolved++
		}
		log.info { "Playlist '$name': resolved ${resolved.size}, unresolved $unresolved of ${paths.size} tracks" }

		val existingId = existing[name]
		val playlistId = existingId ?: create(name, machineId, resolved)
		if (existingId == null) log.info { "Created Plex playlist '$name' ($playlistId)" }

		val current = plex.playlistItems(playlistId).container.metadata
			.associate { it.ratingKey to it.playlistItemID }

		val adds = resolved - current.keys
		val removes = current.filterKeys { it !in resolved }

		adds.chunked(addChunk).forEach { plex.addItems(playlistId, uri(machineId, it)) }
		removes.values.forEach { plex.removeItem(playlistId, it) }
		log.info { "Playlist '$name' ($playlistId) synced: +${adds.size}/-${removes.size}" }

		PlaylistResult(name, added = adds.size, removed = removes.size, unresolved = unresolved)
	}.getOrElse {
		log.error(it) { "Failed to sync playlist $name" }
		PlaylistResult(name, added = 0, removed = 0, unresolved = paths.size, failed = true)
	}

	private fun create(name: String, machineId: String, keys: Set<String>): String =
		plex.createPlaylist(name, uri(machineId, keys.take(addChunk))).container.metadata.first().ratingKey

	private fun uri(machineId: String, keys: Collection<String>) =
		"server://$machineId/com.plexapp.plugins.library/library/metadata/${keys.joinToString(",")}"

	private fun nfc(s: String) = Normalizer.normalize(s, Normalizer.Form.NFC)
}
