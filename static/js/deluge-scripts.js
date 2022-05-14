
let stomp

function delugeTorrents() {
    let sock = new WebSocket(`ws://${global.host}:8133/stream`);
    stomp = Stomp.over(sock);
    stomp.connect({},
        function () {
            console.log("Connected")
            stomp.send("/stream/clear");

            stomp.subscribe('/topic/torrents', function(data) {
                let torrents = JSON.parse(data.body)

                renderTorrents(torrents)
            });
        })
}

function handleScrollAndLoader(torrents) {
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
    let $torrents = $('#torrents');
    $torrents.empty()

    if ($.isEmptyObject(torrents)) {
        addNoResultsCard()
    } else {
        let torrentCards = [];
        $.each(torrents, function (key, value) {
            torrentCards.push(torrentCard(value))
        });
        $torrents.append(torrentCards.join(""))
    }
    handleScrollAndLoader(torrents);
}

function searchDeluge(event) {
    event.preventDefault()
    searchSpinner(true, false, $('#search-divider-icon'))
    let keywords = $('#keywords').val();

    stomp.send("/stream/search", {}, JSON.stringify({'name': keywords }));
}