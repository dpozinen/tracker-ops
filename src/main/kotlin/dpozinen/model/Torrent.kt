package dpozinen.model

import org.jsoup.nodes.Element

class Torrent(val link: String, val name: String) {

    companion object {
        fun from(el: Element) = Torrent(el.attr("href"), el.text())
    }

    override fun toString() = name

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Torrent

        if (link != other.link) return false
        if (name != other.name) return false

        return true
    }

    override fun hashCode(): Int {
        var result = link.hashCode()
        result = 31 * result + name.hashCode()
        return result
    }

}