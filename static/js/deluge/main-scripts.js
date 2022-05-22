let sock
let stomp
let receiving = false

let torrentsState = []
let mutationRequested = false

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
            stomp.subscribe('/topic/torrents/stop', function() {
                receiving = false
                replaceChildrenOf("#play-pause", '<i class="fa-solid fa-play"></i>')
            });
        })
}

function handleScrollAndLoader(torrents) { // todo improve
    if ($('#search-divider-icon .fa-circle-nodes').length === 0) {
        searchSpinner(false, false, $('#search-divider-icon'), '<i class="fa-solid fa-circle-nodes">');
        if (!$.isEmptyObject(torrents))
            $("#results")[0].scrollIntoView()
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

function replaceChildrenOf(parent, withChildren) {
    $(parent).empty().append(withChildren)
}