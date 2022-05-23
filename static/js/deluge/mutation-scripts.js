function mutate(mutation) {
    mutationRequested = true
    stomp.send(`/stream/mutate/${mutation}`);
}

function mutateWBody(mutation, body) {
    mutationRequested = true
    stomp.send(`/stream/mutate/${mutation}`, {}, JSON.stringify(body));
}

function clearMutations() {
    mutate('clear');
    $('[id^="mu-pill-search-"]').remove()
    $('[id^="mu-pill-sort-"]').remove()
    $('[id^="mu-pill-quick-filter-"]').remove()
}

function clearSortMutation(by) {
    mutateWBody('clear/sort', {'by': by});

    $(`#mu-pill-sort-${by}`).remove()
}

function clearSearchMutation(keyword) {
    mutateWBody('clear/search', { 'name': keyword });

    $(`#mu-pill-search-${keyword}`).remove()
}

function addSortMutation(selected) {
    let $selected = $(selected);
    let by = $selected.attr("mu-sort-by");
    let order = 'ASC'
    let payload = { 'by': by, 'order': order }

    $(`#mu-pill-sort-${by}`).remove()
    mutateWBody('sort', payload);

    $('#sort-mutation-pill').after(sortPillInitial(by, order, $selected));
}

function changeSortOrder(by, order, text) {
    let payload = { 'by': by, 'order': order }
    mutateWBody("sort/reverse", payload);

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

    mutateWBody('search', {'name': keywords});

    if (keywords.length === 0) {
        $('[id^="mu-pill-search-"]').remove()
    } else {
        $('#sort-mutation-pill').after(searchPill(keywords));
    }
}

function addQuickFilterMutation(selected) {
    let $selected = $(selected);
    let by = $selected.attr('mu-by');
    let value = $selected.attr('mu-value');
    let operator = $selected.attr('mu-op');

    let filter = {
        by: by,
        value: value,
        operators: [operator]
    }

    $(`#mu-pill-quick-filter-${by}`).remove()
    mutateWBody('filter', filter)
    $('#sort-mutation-pill').after(quickFilterPill(by, $selected));
}

function clearFilterMutation(by) {
    mutateWBody('filter/clear', {'by': by});

    $(`#mu-pill-quick-filter-${by}`).remove()
}

