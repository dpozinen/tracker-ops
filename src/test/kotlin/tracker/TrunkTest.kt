package tracker

import dpozinen.tracker.TrackerOps
import dpozinen.tracker.TrackerParser
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.data.Index.atIndex
import kotlin.test.Test

class TrunkTest {

    @Test
    fun `should search for dune (2021) in 4k`() {
        val torrents = TrackerOps.Trunk().search(listOf("Dune", "2021", "2160p"))

        assertThat(TrackerParser.Trunk().parseSearch(torrents).torrents).hasSize(2)
            .satisfies({
                assertThat(it.link)
                    .isEqualTo("magnet:?xt=urn:btih:F380DF80EAF77BBD91920D10AE66DB49C846EE63&dn=Dune-2021-2160p-BluRay-x265-HEVC-10bit-HDR-AAC-7-1-Tigole-QxR")
            }, atIndex(0))
    }

    @Test
    fun `should search for dune in 4k`() {
        val torrents = TrackerOps.Trunk().search(listOf("Dune", "2160p"))

        assertThat(TrackerParser.Trunk().parseSearch(torrents).torrents).hasSize(5)
    }

}