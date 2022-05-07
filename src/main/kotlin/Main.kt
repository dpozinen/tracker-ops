fun main(args: Array<String>) {
    val read = ReadActions()

    println("Choose tracker")
    val tracker = Tracker.from(read.tracker())

    println("Enter Search Keyword")
    val torrents = tracker.search(read.keyword())

    println(torrents)

    val index = read.torrentIndex()

    val torrent = tracker.select(index)

    println("""
        
        ${torrent.link}
        
    """.trimIndent())
}
