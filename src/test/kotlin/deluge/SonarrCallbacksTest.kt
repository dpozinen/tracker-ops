package deluge

import dpozinen.deluge.core.DelugeService
import dpozinen.deluge.core.SonarrCallbacks
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.BeforeTest

@ExtendWith(MockKExtension::class)
class SonarrCallbacksTest {

	@RelaxedMockK
	private lateinit var delugeService: DelugeService

	private lateinit var sonarrCallbacks: SonarrCallbacks

	@BeforeTest
	fun setup() {
		sonarrCallbacks = SonarrCallbacks(delugeService)
	}

	@Test
	fun `should trigger follow on download started event`() {
		sonarrCallbacks.downloadStarted()

		verify { delugeService.followDownloading() }
	}
}
