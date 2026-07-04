# Skill: backfill the media library (self-improving, interactive, dry-run first)

You run ON the TrueNAS server. Scripts are beside this file: `lib.sh`,
`deluge.sh`, `organize.sh`. Full rationale: `docs/media-cleanup-design.md`.

**Hard rules (never break):**
- NEVER `mv`/`rm` a file that appears in `deluge.sh list` (it's a live seed).
- NEVER delete any copy until its seeding twin is confirmed healthy AND byte-identity checked (`organize.sh identical`).
- NEVER move a torrent onto a dest that already holds its files (no `dont_replace` adoption — we move into EMPTY `.torrents/` dests only).
- Move seeds ONLY via `deluge.sh move`. Hardlink ONLY via `organize.sh hardlink`.

<!-- ========================= BOOTSTRAP ========================= -->
<!-- FIRST RUN ONLY. These scripts were written OFF this server and are unverified
     here. Complete every item, fix the scripts as needed, then DELETE this whole
     block (to END BOOTSTRAP) and commit. -->
## BOOTSTRAP — calibrate before touching real data

1. Path map: `df -h` and `docker inspect ix-deluge-deluge-1 --format '{{range .Mounts}}{{.Source}} -> {{.Destination}}{{println}}{{end}}'`. Confirm `_MAP` in `lib.sh` matches the live container mounts. Fix if drifted.
2. Same-filesystem check (the whole design depends on it): for each library,
   `organize.sh assert-fs /mnt/alyx/Show/.torrents /mnt/alyx/Show` and the Film pair. Must print "OK: same filesystem". If not, `.torrents/` is a child dataset — fix before proceeding (hardlinks would silently become copies).
3. Deluge control: `deluge.sh list | head`. Confirm rows parse as `id  state  location  label  name` and locations look right. Confirm `deluge-console` exposes `move`, `pause`, `resume`, `info` (`deluge.sh` uses them). Fix `dc()`/awk if needed.
4. libtorrent sanity test on ONE throwaway torrent (add a tiny public-domain torrent, let it finish): `deluge.sh move <id> /Downloads/Show/.torrents/__test__`; confirm in the Deluge UI it returns to Seeding at the new path with NO re-download and NO recheck stall. This proves move-of-own-files behaves on this exact build. Remove the test torrent after.
5. PILOT the full sequence on 2–3 real single-file torrents (below), end to end, and confirm each still seeds and the library hardlink resolves (link count 2) before deleting anything.
6. Record any quirk/fix in `README.md` → "Verified quirks". When 1–5 pass, DELETE this BOOTSTRAP block and commit `chore(cleanup): calibrated backfill on server`.
<!-- ===================== END BOOTSTRAP ===================== -->

## What this does
Consolidate the existing ~299 loose/duplicated releases into the tidy library
while every torrent keeps seeding and zero disk is wasted. Interactive, dry-run
first, small batches, verify-before-delete.

## Classify (from `deluge.sh list`)
- Keep rows whose `location` is a Show/Film **root** (`/Downloads/Show`,
  `/Downloads/chute/Show`, `/Downloads/Film`, `/Downloads/chute/Film`) or
  `/Downloads/done`. Ignore `/Downloads/running`.
- Already under `.../.torrents/` → done; skip (or lighter verify — detect
  misplacement, don't assume).
- `name` ends in `.mkv/.mp4/.avi` → single-file episode/movie. Else → pack (folder).

## Safe per-torrent sequence
For each kept torrent, after the agent decides its show/season:
1. Compute dest (container path): `<library>/.torrents/<original release folder or filename>`. This MUST be empty of the torrent's files (it's the seed's new home).
2. `deluge.sh move <id> <dest>` — pause → move_storage → resume.
   - Loose torrents at a Show/Film root are already in the library dataset → same-dataset instant move, keeps seeding, no recheck.
   - `done` torrents are on another dataset → one-time copy; still fine.
3. Confirm it seeds at the new path (`deluge.sh info <id>` → State: Seeding, Download Folder: the new dest).
4. Hardlink each video into the tidy library:
   `organize.sh hardlink <host .torrents file> <host library dest>`
   (single file → `Show/Name/Season NN/file`; pack → keep the pack folder in
   `.torrents/` intact and hardlink each episode into `Season NN/`). Rename in the
   library is fine — the hardlink name is independent of the seed name.
5. Dedupe ONLY now: for each OTHER copy of this content (old Sonarr copy, loose
   root copy, `done` copy) that is NOT in `deluge.sh list`, run
   `organize.sh identical <that copy> <the seed>`; if IDENTICAL, delete it. If it
   DIFFERS (repack/rename) or is a seed, leave it and flag for review.

## Batching & safety
- Produce a DRY-RUN table first: `id | name | -> dest | hardlinks | deletes | confidence`. Low confidence → NEEDS REVIEW, never auto-run.
- Execute in small batches (~5); after each, the user eyeballs Deluge; then continue.
- Append every action to `organizer.log`: `id, old_path, new_path, hardlinks, deleted`.
- Optionally pause-all in Deluge before a batch.

## Non-seed loose files (phase 2)
Files under a library root on disk but ABSENT from `deluge.sh list` are not seeds.
Relocate with plain `mv` into the right `Season NN/` (or hardlink), dedupe freely.
Re-confirm absence from `deluge.sh list` per file before moving.

## Worklist (ongoing)
`organize.sh worklist /mnt/alyx/Show/.torrents` lists files not yet hardlinked
(link count 1) = "hasn't found its place yet" — non-*arr grabs and failed imports.
