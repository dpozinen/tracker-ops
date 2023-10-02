package dpozinen.tracker

class ReadActions {

    fun tracker(): Trackers {
        val tracker = readln()
        return Trackers.from(tracker)
    }

    fun keyword() = readln()

    fun torrentIndex() = readlnOrNull()?.toInt()!!

}