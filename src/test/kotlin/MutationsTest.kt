import dpozinen.deluge.DelugeState
import dpozinen.deluge.Mutation
import dpozinen.deluge.Mutation.By
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test

class MutationsTest {

    private val a = Data.delugeTorrent.copy(name = "ABCF", state = "Seeding", progress = 10)
    private val b = Data.delugeTorrent.copy(name = "DEF", state = "Seeding", progress = 10)
    private val c = Data.delugeTorrent.copy(name = "ABCDEF", state = "Downloading", progress = 10)
    private val d = Data.delugeTorrent.copy(name = "DEG", state = "Error", progress = 10)

    private val state: DelugeState = DelugeState(_torrents = listOf(a, b, c, d))

    @Test
    fun `should search`() {
        val search = Mutation.Search(name = "F")
        val mutated = state.mutate(search)

        assertThat(mutated.torrents)
            .containsExactlyInAnyOrder(b, c, a)
    }

    @Test
    fun `should search within`() {
        val searchOne = Mutation.Search(name = "E")
        val searchTwo = Mutation.Search(name = "F")

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
        val sort = Mutation.Sort(By.NAME)

        val mutated = state.mutate(sort)

        assertThat(mutated.torrents)
            .containsExactly(c, a, b, d)
    }

    @Test
    fun `should sort within`() {
        val sortProgress = Mutation.Sort(By.PROGRESS)
        val sortState = Mutation.Sort(By.STATE)

        val mutatedProgress = state.mutate(sortProgress)

        assertThat(mutatedProgress.torrents).containsExactly(a,b,c,d)

        val mutatedNamesState = mutatedProgress.mutate(sortState)

        assertThat(mutatedNamesState.torrents).containsExactly(c, d, a, b)

        assertThat(
            state.with(linkedSetOf(sortProgress, sortState)).mutate()
        ).isEqualTo(mutatedNamesState)

    }
}