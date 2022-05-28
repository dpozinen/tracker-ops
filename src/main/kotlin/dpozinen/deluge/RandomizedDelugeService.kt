package dpozinen.deluge

import dpozinen.deluge.mutations.Mutation
import dpozinen.deluge.rest.DelugeTorrentConverter
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Service
import kotlin.random.Random

@Profile("dev")
@Service
class RandomizedDelugeService(private val converter: DelugeTorrentConverter) : DelugeService {
    private var state: DelugeState = DelugeState()

    override fun addMagnet(magnet: String) {}

    override fun statefulTorrents(): DelugeTorrents {
        val torrents = generateTorrents()
        val mutated = state.with(torrents).mutate().torrents
        return DelugeTorrents(mutated, statsFrom(torrents, mutated))
    }

    override fun allTorrents() = statefulTorrents().torrents

    override fun mutate(mutation: Mutation) = synchronized(this) { state = state.mutate(mutation) }

    private fun generateTorrents() = (0..100).map { converter.convert(randomize(it)) }

    private fun randomize(id: Int) = mapOf(
        id.toString() to with(mutableListOf<Pair<String, Any>>()) {
            add("eta" to Random.nextLong(0, 1000000))
            add("name" to delugeTorrent.name.substring(0..Random.nextInt(5, 30)))
            add("progress" to Random.nextInt(0, 100))
            add("ratio" to Random.nextDouble(-50.0, 100.0))
            add("state" to listOf("Seeding", "Paused", "Downloading", "Error")[Random.nextInt(0, 3)])
            add("total_uploaded" to Random.nextLong(0, 5949401940790))
            add("upload_payload_rate" to Random.nextLong(0, 54079000))
            add("download_payload_rate" to Random.nextLong(0, 54079000))
            add("time_added" to Random.nextLong(1624819185, 1624829185))
            add("total_wanted" to Random.nextLong(819185, 1624829185))
            add("total_done" to Random.nextLong(0, 8712212443))
            this
        }.toMap()
    ).entries.first()


    private val delugeTorrent = DelugeTorrent(
        id = "ee21ac410a4df9d2a09a97a6890fc74c0d143a0b",
        name = "Rick and Morty Season 1  [2160p AI x265 FS100 Joy]",
        state = "Seeding",
        progress = "100",
        size = "8.11 GiB",
        ratio = "67.9",
        uploaded = "550.96 GiB",
        downloaded = "8.11 GiB",
        eta = "20h 10m",
        downloadSpeed = "",
        uploadSpeed = "0.18 KiB/s",
        date = "28.06.2021"
    )

}