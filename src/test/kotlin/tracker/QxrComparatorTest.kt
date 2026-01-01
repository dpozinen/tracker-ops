package tracker

import dpozinen.tracker.Deserializers
import dpozinen.tracker.Torrent
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class QxrComparatorTest {

	private val comparator = Deserializers.qxrFirstComparator

	@Test
	fun `should be consistent when both contain qxr`() {
		val a = Torrent(name = "Movie A", contributor = "qxr")
		val b = Torrent(name = "Movie B", contributor = "QXR")

		val result1 = comparator.compare(a, b)
		val result2 = comparator.compare(b, a)

		// If compare(a, b) returns negative, compare(b, a) should return positive
		assertTrue(result1 == 0 || (result1 < 0 && result2 > 0) || (result1 > 0 && result2 < 0),
			"Comparator must be antisymmetric: compare(a,b)=$result1, compare(b,a)=$result2")
	}

	@Test
	fun `should be transitive`() {
		val a = Torrent(name = "Movie A", contributor = "qxr")
		val b = Torrent(name = "Movie B", contributor = "another")
		val c = Torrent(name = "Movie C", contributor = "third")

		val ab = comparator.compare(a, b)
		val bc = comparator.compare(b, c)
		val ac = comparator.compare(a, c)

		// If a < b and b < c, then a < c
		if (ab < 0 && bc < 0) {
			assertTrue(ac < 0, "Transitivity violated: a<b && b<c but a>=c")
		}
	}

	@Test
	fun `qxr contributors should come first`() {
		val qxrTorrent = Torrent(name = "Movie", contributor = "qxr")
		val regularTorrent = Torrent(name = "Movie", contributor = "regular")

		val result = comparator.compare(qxrTorrent, regularTorrent)

		assertTrue(result < 0, "QXR torrent should come before regular torrent")
	}

	@Test
	fun `non-empty contributors should come before empty`() {
		val withContributor = Torrent(name = "Movie", contributor = "someone")
		val withoutContributor = Torrent(name = "Movie", contributor = "")

		val result = comparator.compare(withContributor, withoutContributor)

		assertTrue(result < 0, "Torrent with contributor should come before torrent without")
	}

	@Test
	fun `should compare alphabetically when both are regular contributors`() {
		val a = Torrent(name = "Movie", contributor = "Alice")
		val b = Torrent(name = "Movie", contributor = "Bob")

		val result = comparator.compare(a, b)

		assertTrue(result < 0, "Alice should come before Bob alphabetically")
	}

	@Test
	fun `sorting a list should not throw exception`() {
		val torrents = listOf(
			Torrent(name = "Movie 1", contributor = ""),
			Torrent(name = "Movie 2", contributor = "qxr"),
			Torrent(name = "Movie 3", contributor = "QXR"),
			Torrent(name = "Movie 4", contributor = "regular"),
			Torrent(name = "Movie 5", contributor = "another"),
			Torrent(name = "Movie 6", contributor = "")
		)

		// This should not throw IllegalArgumentException: Comparison method violates its general contract
		val sorted = torrents.sortedWith(comparator)

		// QXR torrents should be first
		assertTrue(sorted[0].contributor.lowercase().contains("qxr"))
	}

	@Test
	fun `should handle case insensitive qxr`() {
		val qxrLower = Torrent(name = "Movie", contributor = "qxr")
		val qxrUpper = Torrent(name = "Movie", contributor = "QXR")
		val qxrMixed = Torrent(name = "Movie", contributor = "QxR")
		val regular = Torrent(name = "Movie", contributor = "regular")

		assertTrue(comparator.compare(qxrLower, regular) < 0)
		assertTrue(comparator.compare(qxrUpper, regular) < 0)
		assertTrue(comparator.compare(qxrMixed, regular) < 0)
	}
}
