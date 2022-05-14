import Data.Companion.httpHeaders
import Data.Companion.sessionIdHttpCookie
import dpozinen.deluge.*
import io.mockk.every
import io.mockk.mockk
import org.assertj.core.api.Assertions.assertThat
import org.springframework.http.ResponseEntity
import kotlin.test.Test

class SearchCommandTest {

    private val client: DelugeClient = mockk()
    private val service: DelugeService = DelugeService("", client)
    private val torrentsResponse: Map<String, MutableMap<String, Any>> = (0..10).asSequence()
        .map { "id-$it" to Data.delugeTorrentResponse.entries.first().value.toMutableMap() }
        .toMap()

    @Test
    fun `should search`() {
        val (batchOne, batchTwo) = mock()

        val batchOneSearch = Command.Search("batch one name")
        val batchTwoSearch = Command.Search("batch two name")
        val allSearch = Command.Search("")

        assertThat(service.torrents().map { it.id })
            .containsExactlyInAnyOrderElementsOf(torrentsResponse.map { it.key })

        service.mutate(batchOneSearch)
        assertThat(service.torrents().map { it.id })
            .containsExactlyInAnyOrderElementsOf(batchOne)

        service.mutate(allSearch)
        assertThat(service.torrents().map { it.id })
            .containsExactlyInAnyOrderElementsOf(torrentsResponse.map { it.key })

        service.mutate(batchTwoSearch)
        assertThat(service.torrents().map { it.id })
            .containsExactlyInAnyOrderElementsOf(batchTwo)
    }

    private fun mock(): Pair<Set<String>, Set<String>> {
        val loginResponse = mockk<ResponseEntity<DelugeResponse>>()
        every { client.login() } returns loginResponse
        every { loginResponse.headers } returns httpHeaders()

        val allTorrentIds = torrentsResponse.map { it.key }.toSet()
        val batchOneIds = setOf("id-0", "id-3", "id-4")
        val batchTwoIds = setOf("id-1", "id-2", "id-7")

        appendBatchSearchNameTo(batchOneIds, "batch one name")
        appendBatchSearchNameTo(batchTwoIds, "batch two name")

        everyClientForIds(batchOneIds) returns mockResponseForIds(batchOneIds)
        everyClientForIds(batchTwoIds) returns mockResponseForIds(batchTwoIds)
        everyClientForIds(allTorrentIds) returns mockResponseForIds(allTorrentIds)
        everyClientForIds(setOf()) returns mockResponseForIds(allTorrentIds)

        return Pair(batchOneIds, batchTwoIds)
    }

    private fun everyClientForIds(ids: Set<String>) =
        every { client.torrents(DelugeParams.torrents(ids), sessionIdHttpCookie) }

    private fun appendBatchSearchNameTo(ids: Set<String>, postfix: String) {
        torrentsResponse
            .entries
            .filter { ids.contains(it.key) }
            .forEach { it.value["name"] = "${it.value["name"]} $postfix" }
    }

    private fun mockResponseForIds(ids: Set<String>): ResponseEntity<DelugeResponse> {
        val batchOneResponse = mockk<ResponseEntity<DelugeResponse>>()
        every { batchOneResponse.body } returns only(ids)
        return batchOneResponse
    }

    private fun only(ids: Set<String>): DelugeResponse {
        val torrents = torrentsResponse.entries.asSequence()
            .filter { ids.contains(it.key) }
            .map { it.toPair() }
            .toMap()

        return DelugeResponse(
            result = mapOf("torrents" to torrents),
            id = 123,
            error = null
        )
    }

}