# Media library cleanup tooling

One-time **backfill** to consolidate the existing loose/duplicated releases into
the tidy hardlink layout, without breaking seeds. Runs ON the TrueNAS server (as
the server-side agent). Design: `docs/media-cleanup-design.md`. Procedure:
`backfill.md` (a self-improving skill — read it first; it self-calibrates then
deletes its own bootstrap).

## Files
- `lib.sh` — container↔host path map + `assert_same_fs`. Source-only.
- `deluge.sh` — `list` / `info` / `move` via `deluge-console` in the container.
- `organize.sh` — host-side `shows` / `episodes` / `worklist` / `assert-fs` /
  `identical` / `hardlink`. Only `hardlink` mutates; nothing here deletes.
- `backfill.md` — the interactive, dry-run, pilot-gated procedure.

## Model (why it's safe)
- Steady state is already set up (Deluge routes *arr downloads by label into
  `alyx/{Show,Film}/.torrents/`; Sonarr/Radarr hardlink into the tidy library;
  torrents seed from `.torrents/`). No callback.
- Backfill moves each existing seed into an EMPTY `.torrents/` dest (plain
  `move_storage` of libtorrent's own files → keeps seeding, no recheck), hardlinks
  it into the library, then deletes redundant NON-seed copies only after
  confirming the seed is healthy and byte-identical.

## Preconditions
- `DELUGE_CONTAINER` (default `ix-deluge-deluge-1`).
- `.torrents/` is a SUBFOLDER (not a child dataset) of each library — verify with
  `organize.sh assert-fs`.
- Shared group + group-write + umask across Deluge/Sonarr/Radarr containers.

## Safety
- Dry-run first; small approved batches; pilot on 2–3; verify-before-delete.
- Never delete anything in `deluge.sh list`; never adopt via `dont_replace`.
- All actions logged to `organizer.log`.

## Verified quirks (fill in during bootstrap)
- deluge-console commands present (`move`/`pause`/`resume`/`info`): _____
- libtorrent move-of-own-files reseeds without recheck on this build: _____
- Path map matches live mounts: _____
- `.torrents/` same filesystem as library (assert-fs): _____
