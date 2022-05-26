package dpozinen.deluge

import dpozinen.deluge.mutations.By
import dpozinen.deluge.mutations.Mutation
import dpozinen.deluge.rest.bytesToSize
import dpozinen.deluge.rest.bytesToSpeed
import dpozinen.deluge.rest.round

interface DelugeService {
    fun addMagnet(magnet: String)
    fun torrents(): DelugeTorrents
    fun mutate(mutation: Mutation)

    fun statsFrom(all: List<DelugeTorrent>, mutated: List<DelugeTorrent>): DelugeTorrents.Stats {
        val total = all.size
        val paused = all.count { it.state == "Paused" }
        val downloading = all.count { it.state == "Downloading" }
        val seeding = all.count { it.state == "Seeding" }
        val activeDown = all.count { By.downloadSpeed.comparable(it.downloadSpeed) > 0 }
        val activeUp = all.count { By.uploadSpeed.comparable(it.uploadSpeed) > 0 }

        val totalDownloaded = all.sumOf { By.downloaded.comparable(it.downloaded) }
        val totalUploaded = all.sumOf { By.uploaded.comparable(it.uploaded) }
        val totalRatio = (totalUploaded / totalDownloaded).round(2)

        val downSpeed = all.sumOf { By.downloadSpeed.comparable(it.downloadSpeed) }
        val upSpeed = all.sumOf { By.uploadSpeed.comparable(it.uploadSpeed) }

        return DelugeTorrents.Stats(
            selected = mutated.size, total = total,
            paused, downloading, seeding,
            activeDown, activeUp,
            totalRatio,
            bytesToSize(totalUploaded.round(2)),
            bytesToSize(totalDownloaded.round(2)),
            bytesToSpeed(downSpeed),
            bytesToSpeed(upSpeed)
        )
    }
}