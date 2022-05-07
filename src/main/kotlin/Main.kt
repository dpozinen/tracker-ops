fun main() {
    val read = ReadActions()

    println("Choose tracker")
    val tracker = Tracker.from(read.tracker())

    while (true)
        searchTracker(tracker, read)
}

private fun searchTracker(tracker: Tracker, read: ReadActions) {
    println("Enter Search Keyword")
    val torrents = tracker.search(read.keyword())

    println(torrents)

    if (torrents.torrents.isEmpty()) return

    val index = read.torrentIndex()

    val torrent = tracker.select(index)

    println(
        """
        
        
        
        
        ${torrent.link}
        
        
        
        
    """.trimIndent()
    )
}
