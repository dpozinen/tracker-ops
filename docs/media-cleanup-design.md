# Media Library Cleanup + Hardlink Restructure — Design

Date: 2026-07-04 (rev 2 — hardlink model)

## Goal

Get to a state where the media libraries are tidy (`Show/Name/Season NN/`,
`Film/Name/`), every torrent keeps seeding (including season packs), there are
**zero duplicate copies wasting disk**, and downloads self-organize going
forward. Two workstreams:

- **Part A — Steady-state setup:** restructure so hardlinks work, and let
  Sonarr/Radarr's native hardlink-on-import do the organizing. No custom code in
  the hot path.
- **Part B — One-time backfill:** safely consolidate the existing ~299 loose /
  duplicated releases into the new structure without breaking seeds or losing
  data.

> This supersedes rev 1, which was built on "no hardlinks + a `move_storage`
> callback." Investigation + an adversarial review showed that model was
> over-engineered and rode two known libtorrent bugs. The hardlink model below is
> simpler, standard (TRaSH), and safer.

## Background: current topology (verified)

- TrueNAS SCALE 25.10, Docker. ZFS pools `alyx` and `chute`.
- Deluge (linuxserver 2.2.0), Sonarr + Radarr (home-operations), Plex, Prowlarr.
- **Separate ZFS datasets** (each its own filesystem): `alyx/Show` (4.3 TB, in
  Sonarr), `alyx/Film` (Radarr), `alyx/deluge/running` + `alyx/deluge/done`
  (download staging), `chute/Show` + `chute/Film` (archival, **not** in *arr;
  user evacuates content here manually when low on space).
- Deluge downloads to `alyx` only. ~1062 torrents; ~299 seed from the Show
  library **roots** (loose releases dumped by an old regex "blind mover"). Some
  episodes exist 3× (e.g. Dr. Stone S04E37: seeding copy in `alyx/deluge/done`,
  Sonarr copy in `alyx/Show/Dr. STONE/Season 4/`, loose copy at `alyx/Show/`
  root) — all real byte copies.
- **Root cause of all duplication:** `alyx/deluge/done` and `alyx/Show` are
  **separate ZFS datasets**. Hardlinks cannot cross datasets even in one pool, so
  Sonarr's hardlink attempt hits `EXDEV` and **silently falls back to copy**.

## The model

**Hardlinks make the pack problem and the duplication problem disappear at
once** — one inode, two names (the seed's release name in a hidden index, the
tidy renamed name in the library), one physical copy, seeding preserved. The only
requirement is that the seed and the library live in the **same ZFS dataset**.

### Layout (per-content datasets — chosen to avoid moving the 4.3 TB library)

Keep `alyx/Show` and `alyx/Film` as their existing datasets. Inside **each**, add
a hidden **subfolder** (NOT a child dataset) for the permanent seed index:

```
alyx/Show/                      (dataset)
    .torrents/                  ← hidden seed index; torrents live + seed here
    Dr. STONE/Season 4/…        ← tidy library = hardlinks into .torrents
alyx/Film/                      (dataset)
    .torrents/
    Some Movie (2020)/…
```

Incomplete downloads stay in a **shared** location (all content types mixed) —
e.g. the existing `alyx/deluge/running`. It cannot live inside the Show dataset
because it also holds films and other content.

### Steady-state flow (a show; films are identical via Radarr)

```
1. Deluge downloads (incomplete) into the shared running/ folder.
2. On complete, Deluge "move completed to" the sonarr label's path
   → alyx/Show/.torrents/<release>/     (one-time cross-dataset copy; then seeds here)
3. Sonarr imports: HARDLINKS each episode into alyx/Show/Dr. STONE/Season 4/
   (same dataset → instant, zero extra disk). Rename may be ON.
4. Torrent keeps seeding from alyx/Show/.torrents/<release>/ forever.
```

Result: one inode, seeding from `.torrents/`, hardlinked into a tidy library.

### Routing (already in use today)

Sonarr/Radarr stamp a **label** on every grab (your Deluge already shows
`Label: sonarr`). Deluge's Label plugin maps each label to a "move completed to"
path:

- `sonarr` label → `alyx/Show/.torrents/`
- `radarr` label → `alyx/Film/.torrents/`

So shows and films route themselves; no script decides content type.

### The "worklist" (replaces the visible-`done`-that-empties idea)

With hardlinks, an organized file has **link count 2** (`.torrents` + library); an
**unsorted** one has link count 1. So "what hasn't found its place yet" is:

```
find /mnt/alyx/Show/.torrents -type f -links 1
```

Same "show me the backlog" answer as a `done` worklist, with **no move step and
no risk**. This is what surfaces non-*arr manual grabs and failed imports.

### What's explicitly gone

- **The `zoe` Sonarr callback** — deleted. Steady state needs no custom code;
  Sonarr's native hardlink import does everything. (`GrabSonarrEvent` /
  `downloadStarted` follow behavior can stay if still wanted; the
  `downloadCompleted` move logic is removed.)
- **The legacy regex "blind mover"** — **kept** (user decision). In the old world
  it ran on `/done` alongside Sonarr's import of the *same* `/done` content → that
  double-handling caused the triplication. In the new world *arr content routes to
  `.torrents/` (via label), so it **never lands in `/done`** — the regex mover only
  ever sees the non-*arr grabs it's meant to handle. The two now operate on
  **disjoint** sets, so no more triplication. (Its non-*arr output still lands
  loose and outside the hardlink pipeline — accepted for that low-volume content.)

## Constraints, guardrails, and tradeoffs

- **One mover per content set.** For *arr content, Deluge is the only thing that
  *moves* the seed (into `.torrents/`) and Sonarr/Radarr only *hardlink* — no
  second mover on that path (no callback). The regex mover is allowed **only**
  because it acts on a disjoint set: unlabeled non-*arr grabs in `/done`, which
  *arr content never touches. Invariant to preserve: nothing but Deluge moves
  *arr content, and the regex mover must never touch `.torrents/` or labeled
  content.
- **`.torrents/` MUST be a subfolder of the library dataset, never a child
  dataset.** One accidental `zfs create` silently reintroduces the `EXDEV`
  copy-fallback bug. Guard: assert the library file and its `.torrents/` entry
  share the same device (`stat` → identical `st_dev`) before trusting hardlinks;
  bake this check into the backfill and setup.
- **Rename may be ON.** With hardlinks the library name and the seed name are
  independent names for one inode, so pretty library names no longer threaten
  seeding. (Rev 1 forced rename off; no longer needed.)
- **Snapshot churn (likely moot).** `.torrents/` churns and now lives in the
  library dataset. ZFS snapshots are **per-dataset, all-or-nothing** — you cannot
  exclude a subfolder — so if `alyx/Show` is on a snapshot schedule, deleted
  torrent data gets pinned in snapshots until they expire. Check Data Protection →
  Periodic Snapshot Tasks: if the media datasets aren't snapshotted (typical for
  re-downloadable media), there's nothing to do. If they are, either accept the
  churn or drop the media snapshot schedule.
- **Quota loss.** Downloads-completed now share the library dataset, so you lose a
  separate download quota boundary. Accepted (the user manually evacuates to
  `chute` when low anyway).
- **Permissions.** Deluge (linuxserver PUID/PGID) and Sonarr/Radarr
  (home-operations UID) all write one dataset. Enforce a **shared group +
  group-write + umask 002** so hardlink creation and redundant-copy deletion
  don't hit permission errors (a failed delete silently leaves a duplicate).
- **Non-*arr grabs** have no automatic home — they land in the shared incomplete
  folder / default path with no label, no hardlink, no import. They surface in the
  link-count worklist and need a manual/agent pass. This is inherent: nothing can
  auto-file a download no tool can identify.
- **chute** is archival: no downloads, no hardlink pipeline. It is cleanup-only
  (Part B organizes it in place; its torrents, if any, are handled the same safe
  way).

## Part A — Steady-state setup (one-time config, no code)

Status: mostly done by the user already (callback disabled, `.torrents/` dirs
created). Remaining items marked below.

1. ✅ Create `alyx/Show/.torrents/` and `alyx/Film/.torrents/` as **subfolders**.
   (Still worth a one-time `st_dev` check that they match the library — guards
   against them being child datasets.)
2. Fix container permissions: shared group + group-write + umask 002 so
   Deluge/Sonarr/Radarr can read/write/delete each other's files in the dataset.
   (A failed delete silently leaves a duplicate.)
3. Deluge Label plugin: point `sonarr` "move completed to" → `alyx/Show/.torrents/`,
   `radarr` → `alyx/Film/.torrents/`. **Verified on this box: applying does NOT
   move existing torrents** — so it's safe to set; only new downloads route there,
   and the existing ~299 are handled by Part B.
4. Sonarr/Radarr → Media Management: "Use Hardlinks instead of Copy" ON; confirm
   the download-client path mapping resolves `.torrents/` to the same host path.
5. Snapshots: check whether `alyx/Show`/`alyx/Film` are on a snapshot schedule; if
   yes, accept the `.torrents/` churn or drop the media schedule (can't exclude a
   subfolder). If no schedule → nothing to do.
6. ✅ `zoe` `downloadCompleted` callback disabled.
7. **Regex mover: keep it** (handles non-*arr `/done`; *arr content routes to
   `.torrents/`, never `/done`, so no conflict — see "What's explicitly gone").

Verification: grab one new show, confirm it lands in `.torrents/`, Sonarr
hardlinks it (library file link count 2, same inode), and it keeps seeding.

## Part B — One-time backfill of the existing mess (the risky part)

Scope: the ~299 loose seeding releases at Show/Film roots, the `done`-seeding
duplicates (Dr. Stone class), and non-seed loose files. Both `alyx` and `chute`
libraries.

### The safe per-torrent sequence (avoids the libtorrent hazards)

We do **not** rely on `dont_replace` adoption (buggy on old libtorrent, and the
"recheck immediately after move returns a fake OK" bug can cause data loss).
Instead:

1. Agent determines target show/season for the release (see agent procedure).
2. **Pause** the torrent.
3. `move_storage` the torrent to an **empty** path under the library's
   `.torrents/`.
   - Loose torrents already seed from within the library dataset (`alyx/Show`
     root) → this is an **instant same-dataset move**; libtorrent moves its own
     files, keeps seeding, **no recheck needed**.
   - `done`-based torrents are on a different dataset → this is a one-time
     cross-dataset copy (acceptable; ~84 of them).
4. **Hardlink** each episode from `.torrents/<release>/` into the tidy library
   (`Season NN/`, or `Film/Name/`). Same dataset → instant.
5. **Resume**; confirm the torrent is actually seeding at the new path and the
   library hardlink resolves (link count 2, matching `st_dev`/inode).
6. **Only after that confirmation**, delete redundant **non-seed** copies
   elsewhere (the old Sonarr copy, the loose root copy). Never delete anything
   that appears in Deluge's torrent list; verify byte-identity (size, and a hash
   sample) before deleting a "duplicate."
7. Season packs: `move_storage` the **intact** pack folder into `.torrents/`, then
   hardlink each episode into `Season NN/`. Never split the pack.

### Safety rails

- **Dataset assertion first:** confirm `.torrents/` and the library share
  `st_dev`, or hardlinks silently become copies.
- **libtorrent sanity test:** on ONE throwaway torrent, confirm move + reseed
  behaves on this exact Deluge/libtorrent build before touching real data.
- **Dry-run + interactive batches:** the agent proposes a batch
  (`torrent → target, action, confidence`), the user approves, it executes, the
  user eyeballs Deluge, next batch. Low-confidence → `NEEDS REVIEW`, never
  auto-run.
- **Pilot on 2–3 torrents** end-to-end before any batch.
- **Verify-before-delete, always.** No copy is deleted until its seed twin is
  confirmed healthy and byte-identity is checked. Guards against repacks / renamed
  files whose bytes differ from the torrent.
- **Log** every action (`id, old_path, new_path, hardlinks_made, deleted`) to a
  reversible ledger.
- **Pause-all** optional up front.

### Non-seed loose files (phase 2)

Files present under a library root on disk but **absent** from Deluge's torrent
list are not seeds → relocate with plain `mv` into the right `Season NN/` (or
hardlink + dedupe), freely. Confirm absence from the torrent list per file first.

## Deliverables

- `scripts/cleanup/` — the backfill scripts (Deluge control via `deluge-console`
  in the container; host-side survey/hardlink helpers; the `find -links 1`
  worklist) + a **self-improving skill** (bootstrap block that calibrates the
  off-box scripts against the live server, runs the pilot, then deletes itself;
  steady-state procedure below it).
- One-time setup runbook (Part A) — dataset folders, label paths, *arr hardlink
  settings, snapshot exclusion, permissions, kill-the-regex-mover.

## Open items to verify during implementation

- Exact libtorrent version in linuxserver Deluge 2.2.0 and its `move_storage`
  behavior on this box (pilot test).
- Whether `deluge-console` exposes the needed commands (`move`, `pause`, `resume`,
  `info`); recheck is not needed in the safe path (moves are of libtorrent's own
  files).
- ✅ Deluge Label plugin "apply to existing" behavior — checked on this box:
  applying the label path change triggers **no moves** of existing torrents. Safe.
- Container UID/GID reconciliation across Deluge/Sonarr/Radarr.

## Out of scope

- Merging into one giant `media` dataset (would require copying 7 TB+; per-content
  datasets avoid it).
- Reflinks/block cloning (unreliable cross-dataset; adds nothing over
  same-dataset hardlinks).
- Films' internal layout (flat is correct); `chute` pipeline (archival).
