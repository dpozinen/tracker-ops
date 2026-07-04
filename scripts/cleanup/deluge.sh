#!/usr/bin/env bash
# Deluge control via deluge-console inside the container.
#
#   list                       -> id \t state \t location \t label \t name
#   info <id>                  -> raw torrent block (Name/State/Download Folder/...)
#   move <id> <container-dest> -> pause -> move_storage -> resume -> print state
#
# Design note (see docs/media-cleanup-design.md, Part B):
#   `move` targets an EMPTY dest under .torrents/. libtorrent moves its OWN files,
#   so it keeps seeding and NO force_recheck is needed. We deliberately do NOT rely
#   on dont_replace adoption (buggy on old libtorrent). Never point `move` at a dir
#   that already contains the torrent's files.
set -euo pipefail
source "$(dirname "$0")/lib.sh"

# Run one deluge-console command non-interactively, auto-connecting as localclient.
dc(){ # $1 = console command string
	docker exec "$DELUGE_CONTAINER" sh -c '
		L=$(grep localclient /config/auth | head -1)
		U=${L%%:*}; P=$(printf "%s" "$L" | cut -d: -f2)
		deluge-console "connect 127.0.0.1:58846 $U $P; '"$1"'"
	'
}

cmd_list(){
	# Label appears AFTER Download Folder and may be absent, so accumulate a block
	# and flush on the next "Name:" (and at END).
	dc "info -v" | awk '
		function flush(){ if (name != "") printf "%s\t%s\t%s\t%s\t%s\n", id, state, loc, label, name }
		/^Name: /            { flush(); name=substr($0,7); id=""; state=""; loc=""; label="" }
		/^ID: /              { id=substr($0,5) }
		/^State: /           { split(substr($0,8),a," "); state=a[1] }
		/^Download Folder: / { loc=substr($0,18) }
		/^Label: /           { label=substr($0,8) }
		END                  { flush() }
	'
}

cmd_info(){ dc "info -v $1"; }

cmd_move(){ # $1 = torrent id, $2 = container dest path (must be EMPTY of this torrent's files)
	local id="$1" dest="$2"
	echo ">> pause  $id"        ; dc "pause $id"
	echo ">> move   $id -> $dest"; dc "move $id \"$dest\""
	echo ">> resume $id"        ; dc "resume $id"
	echo ">> state:"            ; dc "info $id" | grep -E '^(Name|State|Download Folder|Progress):'
}

case "${1:-}" in
	list) cmd_list;;
	info) shift; cmd_info "$@";;
	move) shift; cmd_move "$@";;
	*) echo "usage: deluge.sh {list | info <id> | move <id> <container-dest>}" >&2; exit 2;;
esac
