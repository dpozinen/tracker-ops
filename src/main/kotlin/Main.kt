fun main(args: Array<String>) {
    val read = ReadActions()

    val tracker = Tracker.from(read.tracker())

    val torrents = tracker.search(read.keyword())

    println(torrents)

    val index = read.torrentIndex()

    val torrent = tracker.select(index)

    println("""
        
        ${torrent.link}
        
    """.trimIndent())
}
