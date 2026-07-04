#!/usr/bin/env bash
# Shared config + container<->host path translation for the backfill scripts.
# Source this from the other scripts. No side effects on source.

DELUGE_CONTAINER="${DELUGE_CONTAINER:-ix-deluge-deluge-1}"

# Deluge-container path prefix | host path prefix.
# Longer/more-specific prefixes MUST come first.
_MAP=(
	"/Downloads/chute/Show|/mnt/chute/Show"
	"/Downloads/chute/Film|/mnt/chute/Film"
	"/Downloads/Show|/mnt/alyx/Show"
	"/Downloads/Film|/mnt/alyx/Film"
	"/Downloads/done|/mnt/alyx/deluge/done"
	"/Downloads/running|/mnt/alyx/deluge/running"
)

c2h(){ # container path -> host path
	local p="$1" pair c h
	for pair in "${_MAP[@]}"; do
		c="${pair%%|*}"; h="${pair##*|}"
		case "$p" in "$c"*) printf '%s\n' "$h${p#"$c"}"; return;; esac
	done
	printf '%s\n' "$p"
}

h2c(){ # host path -> container path
	local p="$1" pair c h
	for pair in "${_MAP[@]}"; do
		c="${pair%%|*}"; h="${pair##*|}"
		case "$p" in "$h"*) printf '%s\n' "$c${p#"$h"}"; return;; esac
	done
	printf '%s\n' "$p"
}

# Same-filesystem check: two paths must share st_dev for hardlinks to work.
# Exits non-zero (and prints) if they differ or don't exist.
assert_same_fs(){ # $1 $2 (host paths)
	local a b da db
	a="$1"; b="$2"
	[ -e "$a" ] || { echo "assert_same_fs: missing $a" >&2; return 2; }
	[ -e "$b" ] || { echo "assert_same_fs: missing $b" >&2; return 2; }
	da=$(stat -c %D "$a") || return 2
	db=$(stat -c %D "$b") || return 2
	if [ "$da" != "$db" ]; then
		echo "assert_same_fs: DIFFERENT filesystems: $a ($da) vs $b ($db) -> hardlinks will FAIL (EXDEV)" >&2
		return 1
	fi
	return 0
}
