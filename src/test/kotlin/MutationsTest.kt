import dpozinen.deluge.DelugeState
import dpozinen.deluge.mutations.Clear
import dpozinen.deluge.mutations.Mutation.By
import dpozinen.deluge.mutations.Search
import dpozinen.deluge.mutations.Sort
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MutationsTest {

    private val a = Data.delugeTorrent.copy(name = "ABCF", state = "Seeding", progress = 10)
    private val b = Data.delugeTorrent.copy(name = "DEF", state = "Seeding", progress = 10)
    private val c = Data.delugeTorrent.copy(name = "ABCDEF", state = "Downloading", progress = 10)
    private val d = Data.delugeTorrent.copy(name = "DEG", state = "Error", progress = 10)

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
}