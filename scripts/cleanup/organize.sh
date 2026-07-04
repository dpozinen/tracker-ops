#!/usr/bin/env bash
# Host-side, read-mostly helpers for the backfill. The only mutating command is
# `hardlink` (creates a hardlink) — it never deletes. Deletion is a separate,
# deliberate step the agent runs only after a seed is confirmed healthy.
#
#   shows <lib-dir>                 -> existing top-level show folders
#   episodes <dir>                  -> video filenames in a dir (non-recursive-ish)
#   worklist <torrents-dir>         -> files with link count 1 (unsorted: not yet hardlinked)
#   assert-fs <a> <b>               -> exit 0 iff a and b are on the same filesystem
#   identical <a> <b>               -> exit 0 iff same size AND same head+tail 8MB sha
#   hardlink <src> <dest>           -> create hardlink dest -> src (mkdir -p dest dir)
set -euo pipefail
source "$(dirname "$0")/lib.sh"

cmd_shows(){ find "$1" -mindepth 1 -maxdepth 1 -type d -printf '%f\n' 2>/dev/null | sort; }

cmd_episodes(){
	find "$1" -type f \( -iname '*.mkv' -o -iname '*.mp4' -o -iname '*.avi' \) \
		-printf '%f\n' 2>/dev/null | sort
}

cmd_worklist(){ # files in .torrents not yet hardlinked into a library = unsorted
	find "$1" -type f \( -iname '*.mkv' -o -iname '*.mp4' -o -iname '*.avi' \) \
		-links 1 -printf '%p\n' 2>/dev/null | sort
}

cmd_assert_fs(){ assert_same_fs "$1" "$2" && echo "OK: same filesystem"; }

# Cheap byte-identity gate before ANY delete: exact size, plus sha256 of the first
# and last 8 MiB (catches truncation/repacks without hashing whole 1-4GB files).
cmd_identical(){ # $1 $2
	local a="$1" b="$2" sa sb ha hb
	sa=$(stat -c %s "$a") || return 2
	sb=$(stat -c %s "$b") || return 2
	if [ "$sa" != "$sb" ]; then echo "DIFFER: size $sa != $sb"; return 1; fi
	ha=$( { dd if="$a" bs=1M count=8 2>/dev/null; dd if="$a" bs=1M skip=$(( sa/1048576>8 ? sa/1048576-8 : 0 )) 2>/dev/null; } | sha256sum | cut -d' ' -f1)
	hb=$( { dd if="$b" bs=1M count=8 2>/dev/null; dd if="$b" bs=1M skip=$(( sb/1048576>8 ? sb/1048576-8 : 0 )) 2>/dev/null; } | sha256sum | cut -d' ' -f1)
	if [ "$ha" != "$hb" ]; then echo "DIFFER: content hash"; return 1; fi
	echo "IDENTICAL"
}

cmd_hardlink(){ # $1 src (the seed file in .torrents), $2 dest (tidy library path)
	local src="$1" dest="$2"
	[ -f "$src" ] || { echo "hardlink: missing src $src" >&2; return 2; }
	assert_same_fs "$src" "$(dirname "$dest")" || {
		echo "hardlink: src and dest on different filesystems — refusing (would copy, not link)" >&2; return 1; }
	mkdir -p "$(dirname "$dest")"
	if [ -e "$dest" ]; then echo "hardlink: dest exists, skipping: $dest" >&2; return 3; fi
	ln "$src" "$dest"
	echo "linked: $dest  (-> inode $(stat -c %i "$src"), links now $(stat -c %h "$src"))"
}

case "${1:-}" in
	shows)     shift; cmd_shows "$@";;
	episodes)  shift; cmd_episodes "$@";;
	worklist)  shift; cmd_worklist "$@";;
	assert-fs) shift; cmd_assert_fs "$@";;
	identical) shift; cmd_identical "$@";;
	hardlink)  shift; cmd_hardlink "$@";;
	*) echo "usage: organize.sh {shows <dir>|episodes <dir>|worklist <dir>|assert-fs <a> <b>|identical <a> <b>|hardlink <src> <dest>}" >&2; exit 2;;
esac
