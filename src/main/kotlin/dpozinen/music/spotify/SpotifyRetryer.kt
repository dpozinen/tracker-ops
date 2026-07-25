package dpozinen.music.spotify

import feign.RetryableException
import feign.Retryer
import mu.KotlinLogging.logger

/**
 * Retries rate-limited Spotify calls honoring the `Retry-After` Spotify sends on 429.
 * feign's `Retryer.Default` caps that wait at 1s (maxPeriod), which is far too short for
 * real rate limiting — this honors the full delay, up to [maxWaitMs].
 */
class SpotifyRetryer(
	private val maxAttempts: Int = 5,
	private val maxWaitMs: Long = 60_000,
	private val defaultWaitMs: Long = 5_000,
) : Retryer {
	private val log = logger {}
	private var attempt = 1

	override fun continueOrPropagate(e: RetryableException) {
		if (attempt++ >= maxAttempts) {
			log.error { "Spotify ${e.status()} — giving up after $maxAttempts attempts" }
			throw e
		}
		val waitMs = (e.retryAfter()?.minus(System.currentTimeMillis()) ?: defaultWaitMs)
			.coerceIn(0L, maxWaitMs)
		log.warn { "Spotify ${e.status()} rate-limited — waiting ${waitMs}ms then retry ($attempt/$maxAttempts)" }
		try {
			Thread.sleep(waitMs)
		} catch (ie: InterruptedException) {
			Thread.currentThread().interrupt()
			throw e
		}
	}

	override fun clone(): Retryer = SpotifyRetryer(maxAttempts, maxWaitMs, defaultWaitMs)
}
