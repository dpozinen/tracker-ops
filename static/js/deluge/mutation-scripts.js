function clearMutations() {
    stomp.send("/stream/mutate/clear");
    $('[id^="mu-pill-search-"]').remove()
    $('[id^="mu-pill-sort-"]').remove()
}

function clearSortMutation(by) {
    stomp.send("/stream/mutate/clear/sort", {}, JSON.stringify({ 'by': by }));

    $(`#mu-pill-sort-${by}`).remove()
}

function clearSearchMutation(keyword) {
    stomp.send("/stream/mutate/clear/search", {}, JSON.stringify({ 'name': keyword }));

    $(`#mu-pill-search-${keyword}`).remove()
}

function addSortMutation(selected) {
    let $selected = $(selected);
    let by = $selected.attr("mu-sort-by");
    let order = 'ASC'

    let payload = { 'by': by, 'order': order }
    stomp.send("/stream/mutate/sort", {}, JSON.stringify(payload));

    $('#sort-mutation-pill').after(sortPillInitial(by, order, $selected));
}

function changeSortOrder(by, order, text) {
    let payload = { 'by': by, 'order': order }
    stomp.send("/stream/mutate/sort/reverse", {}, JSON.stringify(payload));

    let newOrder = order === 'ASC' ? 'DESC' : 'ASC'
    let arrow = newOrder === 'ASC' ? 'down' : 'up'

    let arrowIcon = sortArrowIcon(arrow, by, newOrder, text)

    $(`#mu-pill-sort-${by}`)
        .empty()
        .append(sortPill(arrowIcon, text, by))
}

function searchDeluge(event) {
    event.preventDefault()
    searchSpinner(true, false, $('#search-divider-icon'))
    let keywords = $('#keywords').val();

    stomp.send("/stream/mutate/search", {}, JSON.stringify({'name': keywords }));

    if (keywords.length === 0) {
        $('[id^="mu-pill-search-"]').remove()
    } else {
        $('#sort-mutation-pill').after(searchPill(keywords));
    }
}

