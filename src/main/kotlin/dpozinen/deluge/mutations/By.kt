package dpozinen.deluge.mutations

import dpozinen.deluge.mutations.By.ByPredicate
import dpozinen.deluge.rest.clients.TorrentsResult.TorrentResult

enum class By {
    NAME,
    STATE,
    SIZE,
    PROGRESS,
    DOWNLOADED,
    RATIO,
    UPLOADED,
    ETA,
    DATE,
    DOWNLOAD_SPEED { override fun property() = "downloadSpeed" },
    UPLOAD_SPEED { override fun property() = "uploadSpeed" };

    open fun property() = name.lowercase()

    fun interface ByPredicate {
        fun test(torrent: TorrentResult): Boolean

        fun or(other: ByPredicate) = ByPredicate { this.test(it) || other.test(it) }
    }

}
