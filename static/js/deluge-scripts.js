
let stomp

function delugeTorrents() {

    let sock = new WebSocket(`ws://${global.host}:8133/stream`);
    stomp = Stomp.over(sock);
    stomp.connect({},
        function () {
            console.log("Connected")
            stomp.subscribe('/topic/torrents', function(data) {
                renderTorrents(data)
            });

        })
}

function search() {
    stomp.send("/stream/search", {}, JSON.stringify({'name': 'Rick' }));
}

function renderTorrents(e) {
    let $torrents = $('#torrents');
    $torrents.empty()

    let torrents = JSON.parse(e.body);

    if ($.isEmptyObject(torrents)) {
        addNoResultsCard()
    } else {
        let torrentCards = [];
        $.each(torrents, function (key, value) {
            torrentCards.push(torrentCard(value))
        });
        $torrents.append(torrentCards.join(""))
    }
    searchSpinner(false, false, $('#search-divider-icon'), '<i class="fa-solid fa-circle-nodes">')
    // $("#results")[0].scrollIntoView()
}