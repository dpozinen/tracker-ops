package deluge

import Data.Companion.delugeTorrentResponse
import dpozinen.deluge.core.DelugeState
import dpozinen.deluge.mutations.By
import dpozinen.deluge.mutations.Filter
import dpozinen.deluge.mutations.Filter.Operator.GREATER
import dpozinen.deluge.mutations.Filter.Operator.IS
import dpozinen.deluge.mutations.Filter.Operator.IS_NOT
import dpozinen.deluge.mutations.Filter.Operator.LESS
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class FilterMutationTest {

    private val a = delugeTorrentResponse.copy(state = "Seeding", downloaded = 550.96, date = 1, eta = 107.0)
    private val b = delugeTorrentResponse.copy(state = "Seeding", downloaded = 1550.96, date = 2, eta = 127.0)
    private val c = delugeTorrentResponse.copy(state = "Downloading", downloaded = 0.0, date = 3, eta = 107.0)
    private val d = delugeTorrentResponse.copy(state = "Error", downloaded = 11111.0, date = 4, eta = 14.0)
    private val e = delugeTorrentResponse.copy(state = "Error", downloaded = 111111.0, date = 5, eta = 101.0)
    private val f = delugeTorrentResponse.copy(state = "Error", downloaded = 22222111111111.0, date = 6, eta = 1.0)

    private val state: DelugeState = DelugeState(_torrents = listOf(a, b, c, d, e, f))

    @Test
    fun `should do nothing if opposite operators (invalid filter)`() {
        val filter = Filter(By.STATE, "Downloading", listOf(IS, IS_NOT))
        val mutated = state.mutate(filter)

        assertThat(mutated.mutations).doesNotContain(filter)
    }

    @Test
    fun `should filter by size greater than or equal to 550 GiB`() {
        val filter = Filter(By.DOWNLOADED, 1550.96, listOf(IS, GREATER))

        val mutated = state.mutate(filter)

        assertThat(mutated.torrents).containsExactlyInAnyOrder(b, d, e, f)
    }

    @Test
    fun `should filter by size greater than 550 GiB`() {
        val filter = Filter(By.DOWNLOADED, 550.96, listOf(GREATER))

        val mutated = state.mutate(filter)

        assertThat(mutated.torrents).containsExactlyInAnyOrder(b, e, d, f)
    }

    @Test
    fun `should filter by size greater than or eq to 11111 GiB and state 'Error'`() {
        val downloaded = Filter(By.DOWNLOADED, 11111.0, listOf(IS, GREATER))
        val error = Filter(By.STATE, "Error")

        val mutated = state.mutate(error).mutate(downloaded)

        assertThat(mutated.torrents).containsExactlyInAnyOrder(d, e, f)
    }

    @Test
    fun `should filter by state`() {
        val filter = Filter(By.STATE, "Error")

        val mutated = state.mutate(filter)

        assertThat(mutated.torrents).containsExactly(d, e, f)
    }

    @Test
    fun `should filter by date`() {
        val filter = Filter(By.DATE, 4L, listOf(GREATER))

        val mutated = state.mutate(filter)

        assertThat(mutated.torrents).containsExactlyInAnyOrder(e, f)
    }

    @Test
    fun `should filter by eta`() {
        val filter = Filter(By.ETA, 107.0, listOf(LESS))

        val mutated = state.mutate(filter)

        assertThat(mutated.torrents).containsExactlyInAnyOrder(e,f,d)
    }

}
