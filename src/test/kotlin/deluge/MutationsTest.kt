package deluge

import Data.Companion.delugeTorrent
import dpozinen.deluge.DelugeState
import dpozinen.deluge.mutations.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class MutationsTest {

    private val a = delugeTorrent.copy(name = "ABCF", state = "Seeding", progress = 10, downloaded = "550.96 GiB", date = "07.10.2010", eta = "10h 7m")
    private val b = delugeTorrent.copy(name = "DEF", state = "Seeding", progress = 10, downloaded = "1550.96 GiB", date = "08.09.2010", eta = "12h 7m")
    private val c = delugeTorrent.copy(name = "ABCDEF", state = "Downloading", progress = 10, downloaded = "0 GiB", date = "10.10.2010", eta = "10m 7s")
    private val d = delugeTorrent.copy(name = "DEG", state = "Error", progress = 10, downloaded = "11111 GiB", date = "10.11.2010", eta = "1d 4h")
    private val e = delugeTorrent.copy(name = "DEG", state = "Error", progress = 10, downloaded = "111111 MiB", date = "10.11.2010", eta = "10d 1h")
    private val f = delugeTorrent.copy(name = "DEG", state = "Error", progress = 10, downloaded = "22222111111111 KiB", date = "10.11.2010", eta = "1s")

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

        assertThat(mutated.torrents).containsExactly(c, e, a, b, d, f)
        assertThat(reversed.torrents).containsExactly(f, d, b, a, e, c)
    }

    @Test
    fun `should sort by date`() {
        val sort = Sort(By.DATE)

        val mutated = state.mutate(sort)
        val reversed = mutated.mutate(sort.reverse())

        assertThat(mutated.torrents).containsExactly(b, a, c, d)
        assertThat(reversed.torrents).containsExactly(d, c, a, b)
    }

    @Test
    fun `should sort by eta`() {
        val sort = Sort(By.ETA)

        val mutated = state.with(listOf(a, b, c, d, e, f)).mutate(sort)
        val reversed = mutated.mutate(sort.reverse())

        assertThat(mutated.torrents).containsExactly(f, c, a, b, d, e)
        assertThat(reversed.torrents).containsExactly(e, d, b, a, c, f)
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
    fun `should clear by id`() {
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

    @Test
    fun `should filter by date`() {
        val filter = Filter(By.DATE, "08.09.2010")

        val mutated = state.mutate(filter)
        val mutatedNot = state.mutate(filter.negate())

        assertThat(mutated.torrents).containsExactly(b)
        assertThat(mutatedNot.torrents).containsExactly(a, c, d)
    }


    @Test
    fun `should filter by state`() {
        val state = DelugeState(listOf(a, b, c, d, e, f))
        val filter = Filter(By.STATE, "Error")

        val mutated = state.mutate(filter)
        val mutatedNot = state.mutate(filter.negate())

        assertThat(mutated.torrents).containsExactly(d,e,f)
        assertThat(mutatedNot.torrents).containsExactly(a, b, c)
    }

    @Test
    fun `should perform 5 mutations on 1000 torrents 5 times per second`() {
        val mutations = By.values().map { Sort(it) }.subList(0, 5).toSet()
        val torrents = (0..1000).map { delugeTorrent.copy(id = it.toString()) }
        val state = DelugeState().with(torrents, mutations)

        val now = Instant.now()
        repeat((0..5).count()) { state.with(torrents).mutate() }
        val after = Instant.now()

        assertThat(Duration.between(now, after)).isLessThan(Duration.ofSeconds(1))
    }
}