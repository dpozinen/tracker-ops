enum class Trackers {
    OneThreeThree;

    companion object {
        fun from(name: String) : Trackers {
            return when (name) {
                "133" -> OneThreeThree
                else -> throw IllegalArgumentException()
            }
        }
    }
}