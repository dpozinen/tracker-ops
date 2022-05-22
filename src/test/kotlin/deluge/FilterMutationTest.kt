package deluge

import Data.Companion.delugeTorrent
import dpozinen.deluge.DelugeState
import dpozinen.deluge.Validator
import dpozinen.deluge.mutations.By
import dpozinen.deluge.mutations.Filter
import dpozinen.deluge.mutations.Filter.Operator.*
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

class FilterMutationTest {

    private val a = delugeTorrent.copy(state = "Seeding", downloaded = "550.96 GiB", date = "07.10.2010", eta = "10h 7m")
    private val b = delugeTorrent.copy(state = "Seeding", downloaded = "1550.96 GiB", date = "08.09.2010", eta = "12h 7m")
    private val c = delugeTorrent.copy(state = "Downloading", downloaded = "0 GiB", date = "10.10.2010", eta = "10m 7s")
    private val d = delugeTorrent.copy(state = "Error", downloaded = "11111 GiB", date = "10.11.2010", eta = "1d 4h")
    private val e = delugeTorrent.copy(state = "Error", downloaded = "111111 MiB", date = "10.11.2010", eta = "10d 1h")
    private val f = delugeTorrent.copy(state = "Error", downloaded = "22222111111111 KiB", date = "10.11.2010", eta = "1s")

    private val validator = Validator()
    private val state: DelugeState = DelugeState(_torrents = listOf(a, b, c, d, e, f))

    @Test
    fun `should invalidate if opposite operators`() {
        val (valid, _) = validator.validate(Filter.Dto(By.STATE, "Downloading", listOf(IS, IS_NOT)))
        assertThat(valid).isFalse
    }

    @Test
    fun `should validate if By doesn't match Operator type`() {
        val (valid, _) = validator.validate(Filter.Dto(By.STATE, "Downloading", listOf(IS)))
        assertThat(valid).isTrue
    }

    @Test
    fun `should filter by size greater than or equal to 550 GiB`() {
        val filter = Filter(By.DOWNLOADED, "550.96 GiB", listOf(IS, GREATER))

        val mutated = state.mutate(filter)

        assertThat(mutated.torrents).containsExactlyInAnyOrder(b, d, a, f)
    }

    @Test
    fun `should filter by size greater than 550 GiB`() {
        val filter = Filter(By.DOWNLOADED, "550.96 GiB", listOf(GREATER))

        val mutated = state.mutate(filter)

        assertThat(mutated.torrents).containsExactlyInAnyOrder(b, d, f)
    }

    @Test
    fun `should filter by size`() {
        val filter = Filter(By.DOWNLOADED, "550.96 GiB", listOf(IS, GREATER))

        val mutated = state.mutate(filter)

        assertThat(mutated.torrents).containsExactlyInAnyOrder(b, d, a, f)
    }

    @Test
    fun `should filter by size greater than 11111 GiB and state 'Error'`() {
        val downloaded = Filter(By.DOWNLOADED, "11111 GiB", listOf(IS, GREATER))
        val error = Filter(By.STATE, "Error")

        val mutated = state.mutate(error).mutate(downloaded)

        assertThat(mutated.torrents).containsExactlyInAnyOrder(f, d)
    }

    @Test
    fun `should filter by state`() {
        val filter = Filter(By.STATE, "Error")

        val mutated = state.mutate(filter)

        assertThat(mutated.torrents).containsExactly(d, e, f)
    }

}
