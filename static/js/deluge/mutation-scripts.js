function clearMutations() {
    stomp.send("/stream/mutate/clear");
}

function clearSortMutation(by) {
    stomp.send("/stream/mutate/clear/sort", {}, JSON.stringify({ 'by': by }));

    $(`#sort-${by}`).remove()
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

    $(`#sort-${by}`)
        .empty()
        .append(sortPill(arrowIcon, text, by))
}
