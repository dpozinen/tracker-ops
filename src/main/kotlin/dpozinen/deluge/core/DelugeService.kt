package dpozinen.deluge.core

import dpozinen.deluge.domain.DelugeTorrent
import dpozinen.deluge.domain.DelugeTorrents
import dpozinen.deluge.mutations.By
import dpozinen.deluge.mutations.Mutation
import dpozinen.deluge.rest.bytesToSize
import dpozinen.deluge.rest.bytesToSpeed
import dpozinen.deluge.rest.round

interface DelugeService {
    fun addMagnet(magnet: String)
    fun statefulTorrents(): DelugeTorrents
    fun allTorrents(): List<DelugeTorrent>
    fun mutate(mutation: Mutation)

    fun info(all: List<DelugeTorrent>, mutated: List<DelugeTorrent>): DelugeTorrents.Info {
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

        return DelugeTorrents.Info(
            selected = mutated.size, total = total,
            downloading, paused, seeding,
            activeDown, activeUp,
            bytesToSize(totalDownloaded.round(2)),
            totalRatio,
            bytesToSize(totalUploaded.round(2)),
            bytesToSpeed(downSpeed),
            bytesToSpeed(upSpeed)
        )
    }
}