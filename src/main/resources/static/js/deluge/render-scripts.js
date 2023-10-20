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
            $('[data-toggle="tooltip"]').tooltip()
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

function addErrCard(msg) {
    let card = `
    <div class="col">
        <div class="card text-center border-danger">
            <div class="card-body">
                <h5 class="card-title">${msg}</h5>
            </div>
        </div>
    </div>
    `

    $('#torrents').empty().append(card)
}

function renderInfo(info) {
    torrentStats = info
    $('#toggle-stats-pill a')
        .html(`<i class="me-2 fa-solid fa-chart-simple"></i> Torrents: ${info.selected}`)

    $.each(torrentStats, function (key, value) {
        let $a = $(`#stats-pill-${key} a`);
        if ($a.length !== 0) {
            let html = $a.html();
            let tmp = html.substring(0, html.lastIndexOf('-->')) + "-->"; // whatever, i hate js

            let val = value === "" || value === "-" || value === "0s" ? ' - ' : value
            $a.empty().append(`${tmp} ${val}`)
        }
    });
}

function showStats() {
    let $selected = $('[id^="stats-pill"]');
    if ($selected[0].hasAttribute('hidden')) {
        $selected.removeAttr('hidden');
    } else {
        $selected.attr('hidden', true);
    }
}

function downloadProvidedMagnets(event) {
    event.preventDefault()
    $.each($('[id^="add-magnets"]'), function (ket, value) {
        let magnet = $(value).val();
        if (magnet) {
            $.ajax({
                url: `http://${global.host}:${global.port}/deluge`,
                method: "POST",
                contentType: "text/plain",
                data: magnet,
            });
        }
    })
    window.location.href = '/deluge.html?sort=UPLOAD_SPEED:DESC&filter=STATE:Downloading:IS'
}