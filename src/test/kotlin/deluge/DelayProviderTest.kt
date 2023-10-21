package deluge

import dpozinen.deluge.core.DelugeDownloadFollower.DelayProvider
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit.SECONDS
import kotlin.time.toJavaDuration

class DelayProviderTest {

    @Test
    fun shouldReturnCorrectDelay() {
        val delayProvider = DelayProvider(
            mapOf(
                1.days.toJavaDuration() to 100,
                2.days.toJavaDuration() to 200,
                365.minutes.toJavaDuration() to 10,
            )
        )
        assertThat(delayProvider.calculate(1.days.toDouble(SECONDS))).isEqualTo(Duration.parse("14m 24s"))
        assertThat(delayProvider.calculate(30.minutes.toDouble(SECONDS))).isEqualTo(180.seconds)
    }
}