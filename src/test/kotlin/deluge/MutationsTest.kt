package deluge

import Data.Companion.delugeTorrentResponse
import dpozinen.deluge.core.DelugeState
import dpozinen.deluge.mutations.By
import dpozinen.deluge.mutations.Clear
import dpozinen.deluge.mutations.Search
import dpozinen.deluge.mutations.Sort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MutationsTest {

    private val a = delugeTorrentResponse.copy(name = "ABCF", state = "Seeding", progress = 10.0, downloaded = 550.96, date = 1L, eta = 105.0)
    private val b = delugeTorrentResponse.copy(name = "DEF", state = "Seeding", progress = 10.0, downloaded = 1550.96, date = 2L, eta = 127.0)
    private val c = delugeTorrentResponse.copy(name = "ABCDEF", state = "Downloading", progress = 10.0, downloaded = 0.0, date = 3L, eta = 106.0)
    private val d = delugeTorrentResponse.copy(name = "DEG", state = "Error", progress = 10.0, downloaded = 11111.0, date = 4L, eta = 14.0)
    private val e = delugeTorrentResponse.copy(name = "DEG", state = "Error", progress = 10.0, downloaded = 111111.0, date = 5L, eta = 101.0)
    private val f = delugeTorrentResponse.copy(name = "DEG", state = "Error", progress = 10.0, downloaded = 22222111111111.0, date = 6L, eta = 1.0)

    private val state: DelugeState = DelugeState(_torrents = listOf(a, b, c, d))

    @Test
    fun `mutate should be idempotent`() {
        val search = Search(name = "F")
        val mutatedOnce = state.mutate(search)
        val mutated = mutatedOnce.mutate(search).mutate(search).mutate(search).mutate()

        assertThat(mutated).isEqualTo(mutated)
    }

    @Test
    fun `should search`() {
        val search = Search(name = "F")
        val mutated = state.mutate(search)

        assertThat(mutated.torrents).containsExactlyInAnyOrder(b, c, a)
    }

    @Test
    fun `should search within`() {
        val searchOne = Search(name = "E")
        val searchTwo = Search(name = "F")

        val mutatedOnce = state.mutate(searchOne)
        val mutatedTwice = mutatedOnce.mutate(searchTwo)

        assertThat(mutatedTwice.torrents)
            .containsExactlyInAnyOrder(b, c)

        assertThat(mutatedTwice).isEqualTo(
            state.with(linkedSetOf(searchOne, searchTwo)).mutate()
        )
    }

    @Test
    fun `should sort`() {
        val sort = Sort(By.NAME)

        val mutated = state.mutate(sort)

        assertThat(mutated.torrents).containsExactly(c, a, b, d)
    }

    @Test
    fun `should sort by size`() {
        val sort = Sort(By.DOWNLOADED)

        val mutated = state.with(listOf(a, b, c, d, e, f)).mutate(sort)
        val reversed = mutated.mutate(sort.reverse())

        assertThat(mutated.torrents).containsExactly(c, a, b, d, e, f)
        assertThat(reversed.torrents).containsExactly(f, e, d, b, a, c)
    }

    @Test
    fun `should sort by date`() {
        val sort = Sort(By.DATE)

        val mutated = state.mutate(sort)
        val reversed = mutated.mutate(sort.reverse())

        assertThat(mutated.torrents).containsExactly(a, b, c, d)
        assertThat(reversed.torrents).containsExactly(d, c, b, a)
    }

    @Test
    fun `should sort by eta`() {
        val sort = Sort(By.ETA)

        val mutated = state.with(listOf(a, b, c, d, e, f)).mutate(sort)
        val reversed = mutated.mutate(sort.reverse())

        assertThat(mutated.torrents).containsExactly(f,d,e,a,c,b)
        assertThat(reversed.torrents).containsExactly(b,c,a,e,d,f)
    }

    @Test
    fun `should sort within`() {
        val sortProgress = Sort(By.PROGRESS)
        val sortState = Sort(By.STATE)

        val mutatedProgress = state.mutate(sortProgress)

        assertThat(mutatedProgress.torrents).containsExactly(a,b,c,d)

        val mutatedNamesState = mutatedProgress.mutate(sortState)

        assertThat(mutatedNamesState.torrents).containsExactly(c, d, a, b)

        assertThat(
            state.with(linkedSetOf(sortProgress, sortState)).mutate()
        ).isEqualTo(mutatedNamesState)

    }

    @Test
    fun `should clear by id leaving only name sort`() {
        val sortProgress = Sort(By.PROGRESS)
        val sortMutations = linkedSetOf(sortProgress, Sort(By.NAME))

        val mutated = state.with(sortMutations)
            .mutate()
            .with(state.torrents)
            .mutate(Clear(sortProgress))

        assertThat(mutated.torrents).containsExactly(c, a, b, d)
    }

    @Test
    fun `should clear all`() {
        val sortMutations = linkedSetOf(Sort(By.NAME), Search("bob"))

        val mutated = state.with(sortMutations)
            .mutate()
            .with(state.torrents)
            .mutate(Clear())

        assertThat(mutated.torrents).containsExactly(a, b, c, d)
    }

}