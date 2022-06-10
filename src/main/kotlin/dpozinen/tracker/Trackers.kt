package dpozinen.tracker

enum class Trackers {
    OneThreeThree, Rarbg;

    companion object {
        fun from(name: String) : Trackers {
            return when (name) {
                "133" -> OneThreeThree
                "rarbg" -> Rarbg
                else -> throw IllegalArgumentException()
            }
        }
    }
}