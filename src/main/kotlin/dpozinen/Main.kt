package dpozinen

import dpozinen.tracker.ReadActions
import dpozinen.tracker.Tracker

fun main() {
    val read = ReadActions()

    println("Choose tracker")
    val tracker = Tracker.from(read.tracker())

    while (true)
        searchTracker(tracker, read)
}

private fun searchTracker(tracker: Tracker, read: ReadActions) {
    println("Enter Search Keyword")
    val keywords = read.keyword()
    val torrents = tracker.search(keywords)

    println(torrents)

    if (torrents.torrents.isEmpty()) return

    val index = read.torrentIndex()

    val torrent = tracker.select(keywords, index)

    println(
        """
        
        
        
        
        ${torrent.link}
        
        
        
        
    """.trimIndent()
    )
}
