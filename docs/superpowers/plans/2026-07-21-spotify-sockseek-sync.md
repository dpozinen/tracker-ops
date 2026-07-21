# Spotify → Sockseek Music Sync Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a scheduled job to the zoe Spring Boot app that enumerates owned Spotify playlists, liked songs, and saved albums, submits each to the sockseek daemon for FLAC download via Soulseek, and reports results via Telegram.

**Architecture:** sockseek runs as a persistent compose service in daemon mode with an HTTP API; zoe calls it via a Feign client alongside a Feign client for the Spotify Web API. A single `@Scheduled` job orchestrates enumeration → sequential submission → Telegram summary.

**Tech Stack:** Kotlin, Spring Boot 3, Spring Cloud OpenFeign, AssertJ + MockK + WireMock (tests)

## Global Constraints

- Follow existing Feign client pattern: `@FeignClient` + inner `open class Config` + `RequestInterceptor` (see `PlexClient.kt`)
- All config under `zoe.music.*` in `application.yml`, injected via `@ConfigurationProperties`
- Credentials from env vars (`${SPOTIFY_CLIENT_ID}` etc.) — never hardcoded
- Sequential source processing only (Soulseek rate-limit safety)
- Retry via Feign's `Retryer.Default()` for network/5xx only; 4xx → immediate Telegram alert, no retry
- Test assertions: `assertThat(actual).isEqualTo(expected)` — no spot-checking with `.contains()`
- Use tabs for indentation

---

### Task 1: Infrastructure — docker-compose + application.yml

**Files:**
- Modify: `docker-compose.yml`
- Modify: `src/main/resources/application.yml`

**Interfaces:**
- Produces: `zoe.music.*` config keys consumed by Tasks 2–6; `sockseek` service on `http://sockseek:5030`

- [ ] **Step 1: Add sockseek service to docker-compose.yml**

Add after the `zoe` service block:

```yaml
  sockseek:
    image: ghcr.io/fiso64/sockseek:0.9.0   # ponytail: pinned — daemon API is experimental
    volumes:
      - /mnt/zoom/apps/sockseek:/config
      - /mnt/music:/music
    command: daemon --server-ip 0.0.0.0 --server-port 5030
    restart: unless-stopped
```

> `sockseek.conf` lives in `/mnt/zoom/apps/sockseek/` on the host. It must contain Spotify credentials, preferred format (FLAC), yt-dlp fallback, and output path `/music`. Create this file on the server separately before first run — sockseek's own docs cover the format.

- [ ] **Step 2: Add zoe.music block to application.yml**

Append at the end of the `zoe:` section (after `health:`):

```yaml
  music:
    cron: '0 0 3 * * *'
    spotify:
      client-id: ${SPOTIFY_CLIENT_ID}
      client-secret: ${SPOTIFY_CLIENT_SECRET}
      refresh-token: ${SPOTIFY_REFRESH_TOKEN}
      include-collaborative: false
      additional-playlists: []
    sockseek:
      url: http://sockseek:5030
```

- [ ] **Step 3: Commit**

```bash
git add docker-compose.yml src/main/resources/application.yml
git commit -m "feat: add sockseek compose service and music config"
```

---

### Task 2: MusicConfig

**Files:**
- Create: `src/main/kotlin/dpozinen/music/MusicConfig.kt`

**Interfaces:**
- Produces: `MusicConfig` bean with `cron`, `spotify: SpotifyConfig`, `sockseek: SockseekConfig`; consumed by Tasks 3, 4, 5, 6

- [ ] **Step 1: Create MusicConfig.kt**

```kotlin
package dpozinen.music

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "zoe.music")
class MusicConfig {
	var cron: String = "0 0 3 * * *"
	var spotify: SpotifyConfig = SpotifyConfig()
	var sockseek: SockseekConfig = SockseekConfig()

	class SpotifyConfig {
		var clientId: String = ""
		var clientSecret: String = ""
		var refreshToken: String = ""
		var includeCollaborative: Boolean = false
		var additionalPlaylists: List<String> = emptyList()
	}

	class SockseekConfig {
		var url: String = "http://sockseek:5030"
	}
}
```

- [ ] **Step 2: Verify it binds — run the app and check startup**

```bash
./gradlew bootRun
```

Expected: app starts without `ConfigurationPropertiesBindException`. If it fails, check that `@Component` is on `MusicConfig` and the yml key casing matches (Spring binds kebab-case to camelCase automatically).

- [ ] **Step 3: Commit**

```bash
git add src/main/kotlin/dpozinen/music/MusicConfig.kt
git commit -m "feat: add MusicConfig ConfigurationProperties"
```

---

### Task 3: SpotifyTokenProvider

**Files:**
- Create: `src/main/kotlin/dpozinen/music/spotify/SpotifyTokenProvider.kt`
- Create: `src/test/kotlin/music/SpotifyTokenProviderTest.kt`

**Interfaces:**
- Consumes: `MusicConfig.SpotifyConfig` (clientId, clientSecret, refreshToken)
- Produces: `SpotifyTokenProvider` — implements `feign.RequestInterceptor`; consumed by Task 4's Feign config

- [ ] **Step 1: Write the failing test**

```kotlin
package music

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import dpozinen.music.MusicConfig
import dpozinen.music.spotify.SpotifyTokenProvider
import feign.RequestTemplate
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test

@WireMockTest(httpPort = 9998)
class SpotifyTokenProviderTest {

	private lateinit var provider: SpotifyTokenProvider

	@BeforeEach
	fun setup() {
		val config = MusicConfig.SpotifyConfig().apply {
			clientId = "test-id"
			clientSecret = "test-secret"
			refreshToken = "test-refresh"
		}
		provider = SpotifyTokenProvider(config, "http://localhost:9998")
	}

	@Test
	fun `injects bearer token from spotify token endpoint`() {
		stubFor(
			post(urlEqualTo("/api/token"))
				.willReturn(okJson("""{"access_token":"tok123","expires_in":3600}"""))
		)

		val template = RequestTemplate()
		provider.apply(template)

		assertThat(template.headers()["Authorization"]).isEqualTo(listOf("Bearer tok123"))
	}

	@Test
	fun `reuses cached token without re-fetching`() {
		stubFor(
			post(urlEqualTo("/api/token"))
				.willReturn(okJson("""{"access_token":"tok123","expires_in":3600}"""))
		)

		val template = RequestTemplate()
		provider.apply(template)
		provider.apply(template)

		verify(1, postRequestedFor(urlEqualTo("/api/token")))
	}
}
```

- [ ] **Step 2: Run to verify it fails**

```bash
./gradlew test --tests "music.SpotifyTokenProviderTest"
```

Expected: compilation error — `SpotifyTokenProvider` does not exist yet.

- [ ] **Step 3: Implement SpotifyTokenProvider**

```kotlin
package dpozinen.music.spotify

import dpozinen.music.MusicConfig
import feign.RequestInterceptor
import feign.RequestTemplate
import mu.KotlinLogging.logger
import org.springframework.web.client.RestClient
import java.time.Instant
import java.util.Base64

class SpotifyTokenProvider(
	private val config: MusicConfig.SpotifyConfig,
	private val tokenBaseUrl: String = "https://accounts.spotify.com",
) : RequestInterceptor {

	private val log = logger {}
	private var token: String = ""
	private var expiresAt: Instant = Instant.MIN

	private val client = RestClient.builder().baseUrl(tokenBaseUrl).build()

	override fun apply(template: RequestTemplate) {
		if (Instant.now().isAfter(expiresAt.minusSeconds(30))) refresh()
		template.header("Authorization", "Bearer $token")
	}

	private fun refresh() {
		val credentials = Base64.getEncoder()
			.encodeToString("${config.clientId}:${config.clientSecret}".toByteArray())

		val response = client.post()
			.uri("/api/token")
			.header("Authorization", "Basic $credentials")
			.header("Content-Type", "application/x-www-form-urlencoded")
			.body("grant_type=refresh_token&refresh_token=${config.refreshToken}")
			.retrieve()
			.body(TokenResponse::class.java)!!

		token = response.accessToken
		expiresAt = Instant.now().plusSeconds(response.expiresIn)
		log.info { "Spotify token refreshed, expires at $expiresAt" }
	}

	private data class TokenResponse(
		val accessToken: String,
		val expiresIn: Long,
	)
}
```

- [ ] **Step 4: Run tests to verify they pass**

```bash
./gradlew test --tests "music.SpotifyTokenProviderTest"
```

Expected: both tests PASS.

- [ ] **Step 5: Commit**

```bash
git add src/main/kotlin/dpozinen/music/spotify/SpotifyTokenProvider.kt \
        src/test/kotlin/music/SpotifyTokenProviderTest.kt
git commit -m "feat: add SpotifyTokenProvider for OAuth token refresh"
```

---

### Task 4: SpotifyClient

**Files:**
- Create: `src/main/kotlin/dpozinen/music/spotify/SpotifyClient.kt`
- Create: `src/test/kotlin/music/SpotifyClientTest.kt`

**Interfaces:**
- Consumes: `SpotifyTokenProvider` (via Feign config), `MusicConfig.SpotifyConfig`
- Produces: `SpotifyClient` with `getMe(): SpotifyUser`, `getPlaylists(limit, offset): SpotifyPagedPlaylists`; consumed by Task 6

- [ ] **Step 1: Write the failing test**

Create `src/test/resources/spotify/me.json`:
```json
{"id": "dpozinen"}
```

Create `src/test/resources/spotify/playlists-page1.json`:
```json
{
  "items": [
    {"id": "pl1", "name": "My Mix", "owner": {"id": "dpozinen"}, "collaborative": false},
    {"id": "pl2", "name": "Someone Else Mix", "owner": {"id": "other"}, "collaborative": false}
  ],
  "next": null
}
```

```kotlin
package music

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import dpozinen.music.spotify.SpotifyClient
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

@WireMockTest(httpPort = 9997)
class SpotifyClientTest {

	private val client: SpotifyClient = Feign.builder()
		.encoder(JacksonEncoder())
		.decoder(JacksonDecoder())
		.target(SpotifyClient::class.java, "http://localhost:9997")

	@Test
	fun `getMe returns user id`() {
		stubFor(get(urlEqualTo("/v1/me")).willReturn(okJson(stringResource("spotify/me.json"))))

		val user = client.getMe()

		assertThat(user.id).isEqualTo("dpozinen")
	}

	@Test
	fun `getPlaylists returns paged response`() {
		stubFor(
			get(urlPathEqualTo("/v1/me/playlists"))
				.withQueryParam("limit", equalTo("50"))
				.withQueryParam("offset", equalTo("0"))
				.willReturn(okJson(stringResource("spotify/playlists-page1.json")))
		)

		val page = client.getPlaylists(50, 0)

		assertThat(page.items).hasSize(2)
		assertThat(page.items[0].owner.id).isEqualTo("dpozinen")
		assertThat(page.next).isNull()
	}

	private fun stringResource(path: String) =
		SpotifyClientTest::class.java.classLoader.getResource(path)!!.readText()
}
```

- [ ] **Step 2: Run to verify it fails**

```bash
./gradlew test --tests "music.SpotifyClientTest"
```

Expected: compilation error — `SpotifyClient` does not exist.

- [ ] **Step 3: Implement SpotifyClient**

```kotlin
package dpozinen.music.spotify

import dpozinen.music.MusicConfig
import feign.Retryer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient(
	name = "spotify",
	url = "https://api.spotify.com/v1",
	configuration = [SpotifyClient.Config::class],
)
interface SpotifyClient {

	@GetMapping("/me")
	fun getMe(): SpotifyUser

	@GetMapping("/me/playlists")
	fun getPlaylists(
		@RequestParam limit: Int = 50,
		@RequestParam offset: Int = 0,
	): SpotifyPagedPlaylists

	open class Config {
		@Bean
		open fun retryer() = Retryer.Default()

		@Bean
		open fun spotifyAuth(
			@Autowired config: MusicConfig,
		) = SpotifyTokenProvider(config.spotify)
	}
}

data class SpotifyUser(val id: String)

data class SpotifyPagedPlaylists(
	val items: List<SpotifyPlaylist>,
	val next: String?,
)

data class SpotifyPlaylist(
	val id: String,
	val name: String,
	val owner: SpotifyOwner,
	val collaborative: Boolean,
)

data class SpotifyOwner(val id: String)
```

- [ ] **Step 4: Register in App.kt**

```kotlin
@EnableFeignClients(
	clients = [
		DelugeAuthClient::class,
		DelugeConnectionClient::class,
		DelugeActionsClient::class,
		PlexClient::class,
		SpotifyClient::class,   // add this
	]
)
```

- [ ] **Step 5: Run tests**

```bash
./gradlew test --tests "music.SpotifyClientTest"
```

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/dpozinen/music/spotify/SpotifyClient.kt \
        src/main/kotlin/dpozinen/App.kt \
        src/test/kotlin/music/SpotifyClientTest.kt \
        src/test/resources/spotify/
git commit -m "feat: add SpotifyClient Feign client"
```

---

### Task 5: SockseekClient

**Files:**
- Create: `src/main/kotlin/dpozinen/music/sockseek/SockseekClient.kt`
- Create: `src/test/kotlin/music/SockseekClientTest.kt`

**Interfaces:**
- Consumes: `MusicConfig.SockseekConfig.url`
- Produces: `SockseekClient` with `submitJob(request: SockseekJobRequest): SockseekJobResponse`; consumed by Task 6

- [ ] **Step 1: Fetch the sockseek OpenAPI spec to confirm the endpoint**

With sockseek daemon running locally (or on the server), fetch:
```bash
curl http://localhost:5030/api/openapi.json | jq '.paths | keys'
```

Identify the correct endpoint for submitting a download job. Most likely `POST /api/download` or `POST /api/jobs`. Update the `@PostMapping` in the implementation below to match.

- [ ] **Step 2: Write the failing test**

Create `src/test/resources/sockseek/job-response.json` based on the actual API response shape you observed in step 1. Example (update to match reality):
```json
{"id": "abc123", "status": "queued"}
```

```kotlin
package music

import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit5.WireMockTest
import dpozinen.music.sockseek.SockseekClient
import dpozinen.music.sockseek.SockseekJobRequest
import feign.Feign
import feign.jackson.JacksonDecoder
import feign.jackson.JacksonEncoder
import org.assertj.core.api.Assertions.assertThat
import kotlin.test.Test

@WireMockTest(httpPort = 9996)
class SockseekClientTest {

	private val client: SockseekClient = Feign.builder()
		.encoder(JacksonEncoder())
		.decoder(JacksonDecoder())
		.target(SockseekClient::class.java, "http://localhost:9996")

	@Test
	fun `submitJob posts source url and returns job response`() {
		stubFor(
			post(urlEqualTo("/api/download"))   // update path if different
				.withRequestBody(matchingJsonPath("$.url", equalTo("https://open.spotify.com/playlist/abc")))
				.willReturn(okJson("""{"id":"abc123","status":"queued"}"""))
		)

		val response = client.submitJob(SockseekJobRequest("https://open.spotify.com/playlist/abc"))

		assertThat(response.id).isEqualTo("abc123")
	}
}
```

- [ ] **Step 3: Run to verify it fails**

```bash
./gradlew test --tests "music.SockseekClientTest"
```

Expected: compilation error — `SockseekClient` does not exist.

- [ ] **Step 4: Implement SockseekClient**

```kotlin
package dpozinen.music.sockseek

import feign.Retryer
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.context.annotation.Bean
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody

@FeignClient(
	name = "sockseek",
	url = "\${zoe.music.sockseek.url}",
	configuration = [SockseekClient.Config::class],
)
interface SockseekClient {

	// ponytail: path confirmed from /api/openapi.json — update if API changes between versions
	@PostMapping("/api/download")
	fun submitJob(@RequestBody request: SockseekJobRequest): SockseekJobResponse

	open class Config {
		@Bean
		open fun retryer() = Retryer.Default()
	}
}

data class SockseekJobRequest(val url: String)

data class SockseekJobResponse(
	val id: String,
	val status: String,
)
```

- [ ] **Step 5: Register in App.kt**

```kotlin
@EnableFeignClients(
	clients = [
		DelugeAuthClient::class,
		DelugeConnectionClient::class,
		DelugeActionsClient::class,
		PlexClient::class,
		SpotifyClient::class,
		SockseekClient::class,   // add this
	]
)
```

- [ ] **Step 6: Run tests**

```bash
./gradlew test --tests "music.SockseekClientTest"
```

Expected: PASS.

- [ ] **Step 7: Commit**

```bash
git add src/main/kotlin/dpozinen/music/sockseek/SockseekClient.kt \
        src/main/kotlin/dpozinen/App.kt \
        src/test/kotlin/music/SockseekClientTest.kt \
        src/test/resources/sockseek/
git commit -m "feat: add SockseekClient Feign client"
```

---

### Task 6: MusicSyncJob

**Files:**
- Create: `src/main/kotlin/dpozinen/music/MusicSyncJob.kt`
- Create: `src/test/kotlin/music/MusicSyncJobTest.kt`

**Interfaces:**
- Consumes:
  - `SpotifyClient.getMe(): SpotifyUser`
  - `SpotifyClient.getPlaylists(limit: Int, offset: Int): SpotifyPagedPlaylists`
  - `SpotifyPagedPlaylists(items: List<SpotifyPlaylist>, next: String?)`
  - `SpotifyPlaylist(id: String, name: String, owner: SpotifyOwner, collaborative: Boolean)`
  - `SpotifyOwner(id: String)`
  - `SockseekClient.submitJob(request: SockseekJobRequest): SockseekJobResponse`
  - `SockseekJobRequest(url: String)`
  - `SockseekJobResponse(id: String, status: String)`
  - `Telegram.sendMessage(chatId: Long, text: String)`
  - `Telegram.chatId: Long`
  - `MusicConfig`

- [ ] **Step 1: Write the failing test**

> Note on Telegram summary: the spec mentions "+N new tracks" per source. `SockseekJobResponse.status` will be "queued" at submission time — not a track count. The summary below reports job status; update to show download counts if the sockseek API response includes them (check the OpenAPI spec in Task 5).

```kotlin
package music

import dpozinen.health.rest.Telegram
import dpozinen.music.MusicConfig
import dpozinen.music.MusicSyncJob
import dpozinen.music.sockseek.SockseekClient
import dpozinen.music.sockseek.SockseekJobRequest
import dpozinen.music.sockseek.SockseekJobResponse
import dpozinen.music.spotify.SpotifyClient
import dpozinen.music.spotify.SpotifyOwner
import dpozinen.music.spotify.SpotifyPagedPlaylists
import dpozinen.music.spotify.SpotifyPlaylist
import dpozinen.music.spotify.SpotifyUser
import feign.FeignException
import feign.Request
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.junit5.MockKExtension
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import kotlin.test.Test

@ExtendWith(MockKExtension::class)
class MusicSyncJobTest {

	@MockK lateinit var spotify: SpotifyClient
	@RelaxedMockK lateinit var sockseek: SockseekClient
	@RelaxedMockK lateinit var telegram: Telegram

	private lateinit var job: MusicSyncJob

	@BeforeEach
	fun setup() {
		val config = MusicConfig().apply {
			spotify = MusicConfig.SpotifyConfig().apply {
				additionalPlaylists = listOf("Extra Mix")
			}
		}
		job = MusicSyncJob(spotify, sockseek, telegram, config)
		every { spotify.getMe() } returns SpotifyUser("me")
		every { sockseek.submitJob(any()) } returns SockseekJobResponse("id1", "queued")
	}

	@Test
	fun `submits owned playlists, liked songs, and albums`() {
		every { spotify.getPlaylists(50, 0) } returns SpotifyPagedPlaylists(
			items = listOf(
				SpotifyPlaylist("pl1", "My Mix", SpotifyOwner("me"), false),
				SpotifyPlaylist("pl2", "Their Mix", SpotifyOwner("other"), false),
			),
			next = null,
		)
		val captured = mutableListOf<SockseekJobRequest>()
		every { sockseek.submitJob(capture(captured)) } returns SockseekJobResponse("id1", "queued")

		job.sync()

		assertThat(captured.map { it.url }).isEqualTo(listOf(
			"https://open.spotify.com/playlist/pl1",
			"spotify:liked",
			"spotify:albums",
		))
	}

	@Test
	fun `includes additional playlists by name regardless of owner`() {
		every { spotify.getPlaylists(50, 0) } returns SpotifyPagedPlaylists(
			items = listOf(
				SpotifyPlaylist("pl1", "My Mix", SpotifyOwner("me"), false),
				SpotifyPlaylist("pl2", "Extra Mix", SpotifyOwner("other"), false),
			),
			next = null,
		)
		val captured = mutableListOf<SockseekJobRequest>()
		every { sockseek.submitJob(capture(captured)) } returns SockseekJobResponse("id1", "queued")

		job.sync()

		assertThat(captured.map { it.url }).contains("https://open.spotify.com/playlist/pl2")
	}

	@Test
	fun `sends telegram alert on 4xx and continues`() {
		every { spotify.getPlaylists(50, 0) } returns SpotifyPagedPlaylists(
			items = listOf(
				SpotifyPlaylist("pl1", "My Mix", SpotifyOwner("me"), false),
				SpotifyPlaylist("pl2", "Bad Mix", SpotifyOwner("me"), false),
			),
			next = null,
		)
		val badUrl = "https://open.spotify.com/playlist/pl2"
		every { sockseek.submitJob(SockseekJobRequest(badUrl)) } throws FeignException.NotFound(
			"not found",
			Request.create(Request.HttpMethod.POST, "http://sockseek/api/download", emptyMap(), null, null, null),
			null,
			emptyMap(),
		)

		job.sync()

		verify { telegram.sendMessage(any(), match { it.contains("Bad Mix") && it.contains("❌") }) }
		verify { sockseek.submitJob(SockseekJobRequest("https://open.spotify.com/playlist/pl1")) }
	}

	@Test
	fun `sends summary telegram message after run`() {
		every { spotify.getPlaylists(50, 0) } returns SpotifyPagedPlaylists(
			items = listOf(SpotifyPlaylist("pl1", "My Mix", SpotifyOwner("me"), false)),
			next = null,
		)

		job.sync()

		verify { telegram.sendMessage(any(), match { it.contains("🎵") && it.contains("My Mix") }) }
	}

	@Test
	fun `paginates through multiple pages`() {
		every { spotify.getPlaylists(50, 0) } returns SpotifyPagedPlaylists(
			items = listOf(SpotifyPlaylist("pl1", "Page 1 Mix", SpotifyOwner("me"), false)),
			next = "https://api.spotify.com/v1/me/playlists?offset=50",
		)
		every { spotify.getPlaylists(50, 50) } returns SpotifyPagedPlaylists(
			items = listOf(SpotifyPlaylist("pl2", "Page 2 Mix", SpotifyOwner("me"), false)),
			next = null,
		)
		val captured = mutableListOf<SockseekJobRequest>()
		every { sockseek.submitJob(capture(captured)) } returns SockseekJobResponse("id1", "queued")

		job.sync()

		assertThat(captured.map { it.url }).contains(
			"https://open.spotify.com/playlist/pl1",
			"https://open.spotify.com/playlist/pl2",
		)
	}
}
```

- [ ] **Step 2: Run to verify it fails**

```bash
./gradlew test --tests "music.MusicSyncJobTest"
```

Expected: compilation error — `MusicSyncJob` does not exist.

- [ ] **Step 3: Implement MusicSyncJob**

```kotlin
package dpozinen.music

import dpozinen.health.rest.Telegram
import dpozinen.music.sockseek.SockseekClient
import dpozinen.music.sockseek.SockseekJobRequest
import dpozinen.music.spotify.SpotifyClient
import dpozinen.music.spotify.SpotifyPlaylist
import feign.FeignException
import mu.KotlinLogging.logger
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class MusicSyncJob(
	private val spotify: SpotifyClient,
	private val sockseek: SockseekClient,
	private val telegram: Telegram,
	private val config: MusicConfig,
) {
	private val log = logger {}

	@Scheduled(cron = "\${zoe.music.cron}")
	fun sync() {
		log.info { "Starting music sync" }
		val myId = spotify.getMe().id
		val playlists = fetchAllPlaylists(myId)

		val results = mutableMapOf<String, String>()

		for (playlist in playlists) {
			val url = "https://open.spotify.com/playlist/${playlist.id}"
			results[playlist.name] = submit(playlist.name, url)
		}
		results["Liked Songs"] = submit("Liked Songs", "spotify:liked")
		results["Saved Albums"] = submit("Saved Albums", "spotify:albums")

		telegram.sendMessage(telegram.chatId, buildSummary(results))
		log.info { "Music sync complete" }
	}

	private fun fetchAllPlaylists(myId: String): List<SpotifyPlaylist> {
		val playlists = mutableListOf<SpotifyPlaylist>()
		var offset = 0
		do {
			val page = spotify.getPlaylists(50, offset)
			for (pl in page.items) {
				if (pl.owner.id == myId || pl.name in config.spotify.additionalPlaylists) {
					if (!pl.collaborative || config.spotify.includeCollaborative) {
						playlists.add(pl)
						log.info { "Including playlist: ${pl.name}" }
					}
				} else {
					log.info { "Skipping playlist: ${pl.name} (owner: ${pl.owner.id})" }
				}
			}
			offset += page.items.size
		} while (page.next != null)
		return playlists
	}

	private fun submit(name: String, url: String): String {
		return try {
			val response = sockseek.submitJob(SockseekJobRequest(url))
			log.info { "Submitted $name → job ${response.id} (${response.status})" }
			response.status
		} catch (e: FeignException.FeignClientException) {
			// 4xx — no retry, alert immediately
			val msg = "❌ Music sync failed: $name — ${e.status()} ${e.message}"
			log.error { msg }
			telegram.sendMessage(telegram.chatId, msg)
			"failed (${e.status()})"
		} catch (e: Exception) {
			// network/5xx exhausted retries
			val msg = "❌ Music sync failed: $name — ${e.message}"
			log.error(e) { msg }
			telegram.sendMessage(telegram.chatId, msg)
			"failed"
		}
	}

	private fun buildSummary(results: Map<String, String>): String {
		val lines = results.entries.joinToString("\n") { (name, status) ->
			if (status.startsWith("failed")) "❌ $name — $status"
			else "✓ $name — $status"
		}
		return "🎵 Music sync complete\n$lines"
	}
}
```

- [ ] **Step 4: Run tests**

```bash
./gradlew test --tests "music.MusicSyncJobTest"
```

Expected: PASS. Fix any MockK capture issues if needed — the test for additional playlists may need a `mutableListOf` slot capture pattern adjustment.

- [ ] **Step 5: Run all tests to check for regressions**

```bash
./gradlew test
```

Expected: all existing tests still PASS.

- [ ] **Step 6: Commit**

```bash
git add src/main/kotlin/dpozinen/music/MusicSyncJob.kt \
        src/test/kotlin/music/MusicSyncJobTest.kt
git commit -m "feat: add MusicSyncJob scheduled orchestrator"
```
