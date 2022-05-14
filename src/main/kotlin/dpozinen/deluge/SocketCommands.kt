package dpozinen.deluge

import com.fasterxml.jackson.annotation.JsonProperty

interface Command {

    fun perform(torrents: List<DelugeTorrent>): List<DelugeTorrent>

    class Clear : Command {
        override fun perform(torrents: List<DelugeTorrent>) = torrents
    }

    class Search(@JsonProperty("name") private val name: String) : Command {
        override fun perform(torrents: List<DelugeTorrent>): List<DelugeTorrent> {
            return if (name.isEmpty())
                torrents
            else
                torrents.asSequence()
                    .filter { nameContains(it) }
                    .toList()
        }

        private fun nameContains(it: DelugeTorrent): Boolean {
            return it.name.contains(this.name)
                    || it.name.lowercase().contains(this.name.lowercase())
                    || it.name.replace(".", " ").lowercase().contains(this.name.lowercase())
        }
    }

    class Filter(by: By, value: String)

    class Order(by: By, sort: Sort = Sort.ASC) {
        enum class Sort {
            ASC, DESC
        }
    }

    enum class By {
        state,
        size,
        downloaded,
        ratio,
        uploaded,
        downloadSpeed,
        eta,
        uploadSpeed,
        date
    }
}
