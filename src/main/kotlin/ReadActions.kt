class ReadActions {

    fun tracker(): Trackers {
        val tracker = readLine()!!
        return Trackers.from(tracker)
    }

    fun keyword() = readLine()?.split(" ")!!

    fun torrentIndex() = readLine()?.toInt()!!

}