# Spotify → Sockseek Music Sync — Design

Date: 2026-07-20

## Goal

A scheduled Spring Boot job that enumerates owned Spotify playlists, liked songs,
saved albums, and any explicitly listed additional playlists — then hands each to
the sockseek daemon (running as a compose service) for FLAC download via Soulseek.
Failures and sync summaries are reported via the existing Telegram integration.

## Architecture

```
MusicSyncJob (@Scheduled)
  ├── SpotifyClient (Feign)     — enumerate sources
  ├── SockseekClient (Feign)    — submit download jobs to daemon
  └── Telegram                  — failure alerts + run summary
```

sockseek runs as a persistent Docker compose service in daemon mode, keeping the
Soulseek connection alive between runs. The Spring app submits jobs via HTTP.

## New Files

```
src/main/kotlin/dpozinen/music/
  MusicSyncJob.kt              — @Scheduled orchestrator
  MusicConfig.kt               — @ConfigurationProperties for zoe.music.*
  spotify/
    SpotifyClient.kt           — @FeignClient for Spotify Web API
    SpotifyTokenProvider.kt    — OAuth access token refresh/cache (RequestInterceptor)
  sockseek/
    SockseekClient.kt          — @FeignClient for sockseek daemon HTTP API
```

`App.kt` — add `SpotifyClient`, `SockseekClient` to `@EnableFeignClients`.

## Config

### application.yml addition

```yaml
zoe:
  music:
    cron: '0 0 3 * * *'
    spotify:
      client-id: ${SPOTIFY_CLIENT_ID}
      client-secret: ${SPOTIFY_CLIENT_SECRET}
      refresh-token: ${SPOTIFY_REFRESH_TOKEN}
      include-collaborative: false
      additional-playlists:
        - "Playlist Name One"
        - "Playlist Name Two"
    sockseek:
      url: http://sockseek:5030
```

### docker-compose addition

```yaml
sockseek:
  image: ghcr.io/fiso64/sockseek:x.x.x   # pin — daemon API is experimental
  volumes:
    - /mnt/music:/music
    - /mnt/zoom/apps/sockseek:/config      # sockseek.conf (Spotify creds, format prefs)
  command: daemon --server-ip 0.0.0.0 --server-port 5030
  restart: unless-stopped
```

Spotify credentials and sockseek download preferences (prefer FLAC, yt-dlp fallback,
output path, Plex-compatible name format) live in `sockseek.conf` on the host volume
rather than in CLI flags — keeps the compose file stable.

## MusicSyncJob Flow

```
1. SpotifyClient.getPlaylists() — paginated, all pages
      filter: owner.id == me              → owned playlists
              OR name in additional-playlists  → explicitly listed
              log skipped at INFO
   + "spotify:liked" (Saved Tracks)
   + "spotify:albums" (Saved Albums)

2. For each source (sequential — Soulseek rate-limit safe):
     SockseekClient.submit(sourceUrl)
       Feign Retryer handles network/5xx retries (configured in SockseekClient config)
       on 4xx               → no retry; Telegram failure alert immediately

3. Telegram end-of-run summary (one message):
     🎵 Music sync complete
     Liked Songs: +12 new
     My Playlist: +3 new
     Road Trip: ✓ (nothing new)
     Saved Albums: +5 new
     ❌ Failed: Summer Vibes — 404 Not Found
```

## SpotifyTokenProvider

Implements Feign `RequestInterceptor` (same pattern as `PlexAuthInterceptor`).
Caches the access token with its expiry timestamp. On each request, refreshes via
`POST https://accounts.spotify.com/api/token` (grant_type=refresh_token) if
expired or missing. Injects `Authorization: Bearer <token>` header.

## SockseekClient

`@FeignClient` pointing at `${zoe.music.sockseek.url}`. Exact endpoint(s) to be
confirmed from the sockseek OpenAPI spec (`GET /api/openapi.json`) at
implementation time — the daemon API is experimental and not yet documented in
prose. Implementation should pin the sockseek image version and note the API
contract in a comment.

## Telegram Notifications

Reuses `Telegram.sendMessage()` directly (already a Spring bean). Two message types:

- **Immediate failure** (4xx or exhausted retries):
  `❌ Music sync failed: [source name] — [status / error]`

- **End-of-run summary**: one message per run, lists every source with new-track
  count or failure reason. Diff sourced from sockseek API response (tracks
  downloaded this run).

## What Is Not In Scope

- Tagging/beets pass for untagged Soulseek rips — separate concern.
- Removing tracks from Spotify after download (`--remove-from-source`).
- Parallel job submission — sequential is intentional (Soulseek rate limits).
