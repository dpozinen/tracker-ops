package dpozinen.deluge.domain

data class Stats(
    private val torrents: List<DelugeTorrent>,
    private val stats: Map<String, List<DataPoint>>
)