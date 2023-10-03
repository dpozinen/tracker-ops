package dpozinen.deluge.core

import dpozinen.deluge.domain.DelugeTorrent
import dpozinen.deluge.domain.DelugeTorrents
import dpozinen.deluge.mutations.Mutation
import dpozinen.deluge.rest.bytesToSize
import dpozinen.deluge.rest.bytesToSpeed
import dpozinen.deluge.rest.clients.TorrentsResult.TorrentResult
import dpozinen.deluge.rest.round

interface DelugeService {
    fun addMagnet(magnet: String)
    fun statefulTorrents(): DelugeTorrents
    fun allTorrents(): List<DelugeTorrent>
    fun rawTorrents(): List<TorrentResult>
    fun mutate(mutation: Mutation)

    fun info(all: List<TorrentResult>, mutated: List<TorrentResult>): DelugeTorrents.Info {
        val paused = all.count { it.state == "Paused" }
        val downloading = all.count { it.state == "Downloading" }
        val seeding = all.count { it.state == "Seeding" }
        val activeDown = all.count { it.downloadSpeed > 0 }
        val activeUp = all.count { it.uploadSpeed > 0 }

        val totalDownloaded = all.sumOf { it.downloaded }
        val totalUploaded = all.sumOf { it.uploaded }
        val totalRatio = (totalUploaded / totalDownloaded).round(2)

        val downSpeed = all.sumOf { it.downloadSpeed }
        val upSpeed = all.sumOf { it.uploadSpeed }

        return DelugeTorrents.Info(
            selected = mutated.size, total = all.size,
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