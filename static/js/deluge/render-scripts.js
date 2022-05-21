function renderTorrents(torrents) {
    if (mutationRequested) {
        mutationRequested = false;

        let $torrents = $('#torrents');
        $torrents.empty()

        if ($.isEmptyObject(torrents)) {
            addNoResultsCard();
        } else {
            let torrentCards = [];
            $.each(torrents, function (key, value) {
                torrentCards.push(torrentCard(value));
            });
            $torrents.append(torrentCards.join(""));
        }

        handleScrollAndLoader(torrents);
        torrentsState = torrentsAsMap(torrents)
    } else {
        $.each(torrents, function (i) {
            let $card
            let incoming = torrents[i]
            let stateful = torrentsState[incoming.id]

            if (typeof stateful === 'undefined') { // new torrent matched mutation criteria -> rerender all
                mutationRequested = true
                renderTorrents(torrents)
            }

            partialTorrentRerender(stateful, incoming, $card);
        })
        torrentsState = torrentsAsMap(torrents)
    }
}

function partialTorrentRerender(stateful, incoming, $card) {
    if (stateful.name !== incoming.name) {
        $card = findOrReturnTorrentCard(incoming, $card);
        $('.tor-name', $card).text(incoming.name);
    }
    if (stateful.size !== incoming.size) {
        $card = findOrReturnTorrentCard(incoming, $card)
        $('.tor-size', $card).html(sizeField(incoming))
    }
    if (stateful.ratio !== incoming.ratio) {
        $card = findOrReturnTorrentCard(incoming, $card)
        $('.tor-ratio', $card).html(ratioField(incoming));
    }
    if (stateful.uploaded !== incoming.uploaded) {
        $card = findOrReturnTorrentCard(incoming, $card)
        $('.tor-uploaded', $card).html(uploadedField(incoming));
    }
    if (stateful.eta !== incoming.eta) {
        $card = findOrReturnTorrentCard(incoming, $card)
        $('.tor-eta', $card).html(etaField(incoming));
    }
    if (stateful.downloadSpeed !== incoming.downloadSpeed) {
        $card = findOrReturnTorrentCard(incoming, $card)
        $('.tor-downloadSpeed', $card).html(downloadSpeedField(incoming));
    }
    if (stateful.uploadSpeed !== incoming.uploadSpeed) {
        $card = findOrReturnTorrentCard(incoming, $card)
        $('.tor-uploadSpeed', $card).html(uploadSpeedField(incoming));
    }
    if (stateful.progress !== incoming.progress || stateful.state !== incoming.state) {
        $card = findOrReturnTorrentCard(incoming, $card)
        $('.tor-progress', $card).html(progressStateField(incoming));
    }
}

function findOrReturnTorrentCard(torrent, $card) {
    if (typeof $card === 'undefined') {
        return $(`#tor-${torrent.id}`);
    } else {
        return $card
    }
}