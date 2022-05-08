package dpozinen.core

class ReadActions {

    fun tracker(): Trackers {
        val tracker = readLine()!!
        return Trackers.from(tracker)
    }

    fun keyword() = readLine()!!

    fun torrentIndex() = readLine()?.toInt()!!

}