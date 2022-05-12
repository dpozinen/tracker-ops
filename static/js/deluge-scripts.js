
function delugeTorrents() {
    searchSpinner(true, false, $('#search-divider-icon'), '<i class="fa-solid fa-circle-nodes">')
    $.ajax({
        type: "GET",
        dataType: "json",
        url: `http://${global.host}:8133/deluge/torrents`,
        success: function (torrents) {
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
            searchSpinner(false, false, $('#search-divider-icon'), '<i class="fa-solid fa-circle-nodes">')
            // $("#results")[0].scrollIntoView()
        }
    });
}