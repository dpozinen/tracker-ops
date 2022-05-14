package dpozinen.deluge

interface Command {

    fun perform(torrents: List<DelugeTorrent>): List<DelugeTorrent>

    class Search(private val name: String) : Command {
        override fun perform(torrents: List<DelugeTorrent>): List<DelugeTorrent> {
            return if (name.isEmpty())
                torrents
            else
                torrents.asSequence()
                    .filter { it.name.contains(this.name) }
                    .toList()
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
