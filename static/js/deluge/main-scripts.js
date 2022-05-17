let sock
let stomp
let receiving = false

window.addEventListener('beforeunload', event => {
    stomp.send("/stream/stop");
    sock._close()
});

function openSocket() {
    sock = new WebSocket(`ws://${global.host}:8133/stream`);
}

function delugeTorrents() {
    searchSpinner(true, false, $('#search-divider-icon'))
    openSocket();
    stomp = Stomp.over(sock);
    stomp.connect({},
        function () {
            console.log("Connected")
            stomp.send("/stream/mutate/clear");
            addSortMutation($('[mu-sort-by=NAME]'))

            stomp.send("/stream/commence");
            receiving = true

            stomp.subscribe('/topic/torrents', function(data) {
                let torrents = JSON.parse(data.body)

                renderTorrents(torrents)
            });
        })
}

function handleScrollAndLoader(torrents) { // todo improve
    setTimeout(function () {
            if ($('#search-divider-icon .fa-circle-nodes').length === 0) {
                searchSpinner(false, false, $('#search-divider-icon'), '<i class="fa-solid fa-circle-nodes">');
                if (!$.isEmptyObject(torrents))
                    $("#results")[0].scrollIntoView()
            }
        },
        5000
    )
}

function renderTorrents(torrents) {
    console.time("render");
    let $torrents = $('#torrents');
    $torrents.empty()


    if ($.isEmptyObject(torrents)) {
        addNoResultsCard()
    } else {
        console.time("gen");
        let torrentCards = [];
        let i = 3
        $.each(torrents, function (key, value) {
            if (i === 3) {
                // torrentCards.push('<div class="row justify-content-center row-cols-auto g-3 mb-3">')
            }
            --i
            torrentCards.push(torrentCard(value));
            if (i === 0) {
                // torrentCards.push('</div>')
                i = 3
            }
        });
        $torrents.append(torrentCards.join(""))
        console.timeEnd("gen");
    }
    handleScrollAndLoader(torrents);
    console.timeEnd("render");
}

function playPause() {
    if (sock.readyState === WebSocket.CLOSED || sock.readyState === WebSocket.CLOSING) {
        openSocket()
        receiving = false
    }

    if (receiving) {
        stomp.send("/stream/stop");
        receiving = false
        replaceChildrenOf("#play-pause", '<i class="fa-solid fa-play"></i>')
    } else {
        stomp.send("/stream/commence");
        receiving = true
        replaceChildrenOf("#play-pause", '<i class="fa-solid fa-pause"></i>')
    }
}

function replaceChildrenOf(parent, withChildren) {
    $(parent).empty().append(withChildren)
}