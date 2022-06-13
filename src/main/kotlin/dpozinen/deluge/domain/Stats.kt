package dpozinen.deluge.domain

data class Stats(
    val torrents: List<DelugeTorrent>,
    val stats: Map<String, List<DataPoint>>,
    val intervals: List<String>
)