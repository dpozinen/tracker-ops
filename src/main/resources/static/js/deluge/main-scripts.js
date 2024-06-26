let sock
let stomp
let receiving = false

let torrentsState = []
let torrentStats = []
let mutationRequested = false

window.addEventListener('beforeunload', () => {
    stomp.send("/stream/stop");
    sock._close()
});

function openSocket() {
    sock = new WebSocket(`ws://${global.host}:${global.port}/stream`);
    sock.onclose = function () {
        receiving = false
        replaceChildrenOf("#play-pause", '<i class="fa-solid fa-play"></i>')
    };
}

function delugeTorrents() {
    $('[data-toggle="tooltip"]').tooltip()
    searchSpinner(true, false, $('#search-divider-icon'))
    openSocket();
    stomp = Stomp.over(sock);
    stomp.connect({},
        function () {
            console.log("Connected")
            stomp.send("/stream/mutate/clear");
            initMutations();

            stomp.send("/stream/commence");
            receiving = true

            stomp.subscribe('/topic/torrents', function(data) {
                let msg = JSON.parse(data.body);
                let torrents = msg.torrents

                renderTorrents(torrents)
                renderInfo(msg.info)
            });

            stomp.subscribe('/topic/torrents/stop', function(data) {
                handleStreamStop(data)
            });
        })
}

function handleScrollAndLoader(torrents) { // todo improve
    if ($('#search-divider-icon .fa-circle-nodes').length === 0) {
        searchSpinner(false, false, $('#search-divider-icon'), '<i class="fa-solid fa-circle-nodes">');
        if (!$.isEmptyObject(torrents))
            $("#toggle-stats-pill")[0].scrollIntoView()
    }
}

function torrentsAsMap(torrents) {
    let map = {}

    $.each(torrents, function(elem, innerJson) {
        let id = innerJson.id
        map[id] = innerJson
    });
    return map
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

function handleStreamStop(data) {
    receiving = false
    replaceChildrenOf("#play-pause", '<i class="fa-solid fa-play"></i>')
    let close = JSON.parse(data.body)
    console.log(close)
    if (close.err) {
        $("#search-divider-icon").attr('title', close.err).addClass("text-danger")
        replaceChildrenOf("#search-divider-icon",
            '<i class="fa-solid fa-circle-exclamation"></i>')

        addErrCard(close.err)
    }
}

function replaceChildrenOf(parent, withChildren) {
    $(parent).empty().append(withChildren)
}