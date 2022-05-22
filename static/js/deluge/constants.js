function infinityIfEmpty(value) {
    let empty = value === "" || value === "-" || value === "0s"
    return empty ? '<i class="fa-solid fa-infinity"></i>' : value
}

function torrentCard(torrent) {
    return `
        <div class="col-10" id="tor-${torrent.id}">
            <div class="card h-100">
                <div class="card-body">
                    <div class="card-title">
                        <div class="progress tor-progress" style="height:20px">
                            ${progressStateField(torrent)}
                        </div>
                    </div>
                    <p class="card-text tor-name">${torrent.name}</p>
                    <div class="row mb-3">
                        <div class="col-4">
                            <h6 class="card-subtitle mb-2 text-danger t-size tor-size">
                                <i class="fa-solid fa-arrow-down"></i>      ${torrent.size}</h6>
                        </div>
                        <div class="col-4">
                            <h6 class="card-subtitle mb-2 text-warning t-size tor-ratio">
                                <i class="fa-solid fa-arrows-up-down"></i>      ${torrent.ratio}</h6>
                        </div>
                        <div class="col-4">
                            <h6 class="card-subtitle mb-2 text-success t-size tor-uploaded">
                                <i class="fa-solid fa-arrow-up"></i>      ${torrent.uploaded}</h6>
                        </div>
                    </div>
                    <div class="row mb-3">
                        <div class="col-4">
                            <h6 class="card-subtitle mb-2 text-muted t-size tor-downloadSpeed">
                                <i class="fa-solid fa-arrow-down"></i>      ${infinityIfEmpty(torrent.downloadSpeed)}</h6>
                        </div>
                        <div class="col-4">
                            <h6 class="card-subtitle mb-2 text-muted t-size tor-eta">
                                <i class="fa-solid fa-clock"></i>      ${infinityIfEmpty(torrent.eta)}</h6>
                        </div>
                        <div class="col-4">
                            <h6 class="card-subtitle mb-2 text-muted t-size tor-uploadSpeed">
                                <i class="fa-solid fa-arrow-up"></i>      ${infinityIfEmpty(torrent.uploadSpeed)}</h6>
                        </div>
                    </div>
                    <h6 class="card-text text-center t-date"><small class="text-muted tor-date">${torrent.date}</small></h6>
                    <a hidden class="btn btn-primary"><i class="fa-solid fa-magnet"></i></a>
                    <button hidden type="button" class="btn btn-outline-primary copy-magnet">
                        <i class="fa-solid fa-copy"></i>
                        <i class="fa-solid fa-magnet"></i> 
                    </button>
                    <button hidden type="button" class="btn btn-outline-primary manual-magnet">
                        <i class="fa-solid fa-gears"></i>
                        <i class="fa-solid fa-magnet"></i>
                    </button>
                </div>
            </div>
        </div>
            `
         // todo pause/play button on torrent
}

function sortArrowIcon(arrow, by, newOrder, text) {
    return `<i class="sort-icon fa-solid fa-arrow-${arrow}-wide-short me-1"
                            onclick="changeSortOrder('${by}', '${newOrder}', '${text.trim()}')">
                    </i>`;
}

function sortPill(arrowIcon, text, by) {
    return `
            <a class="text-light fw-lighter nav-link active">
                        ${arrowIcon}
                        ${text}
               <i onclick="clearSortMutation('${by}')" class="fa-solid fa-circle-minus ms-1"></i>
            </a>
            `;
}

function sortPillInitial(by, order, $selected) {
    return `
        <li class="nav-item border border-light border-1 m-1" id="mu-pill-sort-${by}">
                <a class="text-light fw-lighter nav-link active">
                        <i class="sort-icon fa-solid fa-arrow-down-wide-short me-1"
                            onclick="changeSortOrder('${by}', '${order}', '${$selected.text().trim()}')"
                        ></i>
                        ${$selected.text()}   
                       <i onclick="clearSortMutation('${by}')" class="fa-solid fa-circle-minus ms-1"></i>
                </a>
        </li>
        `;
}

function searchPill(keyword) {
    return `
        <li class="nav-item border border-light border-1 m-1" id="mu-pill-search-${keyword}">
                <a class="text-light fw-lighter nav-link active">
                       <i class="fa-solid fa-magnifying-glass"></i>
                        ${keyword}   
                       <i onclick="clearSearchMutation('${keyword}')" class="fa-solid fa-circle-minus ms-1"></i>
                </a>
        </li>
        `;
}

function sizeField(torrent) {
    return `<i class="fa-solid fa-arrow-down"></i>      ${torrent.size}</h6>`
}

function ratioField(torrent) {
    return `<i class="fa-solid fa-arrows-up-down"></i>      ${torrent.ratio}</h6>`
}

function uploadedField(torrent) {
    return `<i class="fa-solid fa-arrow-up"></i>      ${torrent.uploaded}</h6>`
}

function downloadSpeedField(torrent) {
    return `<i class="fa-solid fa-arrow-down"></i>      ${infinityIfEmpty(torrent.downloadSpeed)}</h6>`
}

function etaField(torrent) {
    return `<i class="fa-solid fa-clock"></i>      ${infinityIfEmpty(torrent.eta)}</h6>`
}

function uploadSpeedField(torrent) {
    return `<i class="fa-solid fa-arrow-up"></i>      ${infinityIfEmpty(torrent.uploadSpeed)}</h6>`
}

function progressStateField(torrent) {
    return `<div class="progress-bar ${stateColor(torrent)}" 
                 style="width:${torrent.progress}%">${torrent.progress}%</div>`
}

function stateColor(torrent) {
    if (torrent.state === 'Seeding') {
        return "bg-success progress-bar-striped progress-bar-animated";
    } else if (torrent.state === 'Downloading') {
        return "progress-bar-striped progress-bar-animated";
    } else if (torrent.state === 'Paused') {
        return "bg-warning";
    } else {
        return "bg-danger";
    }
}

function quickFilterPill(by, $selected) {
    return `
        <li class="nav-item border border-light border-1 m-1" id="mu-pill-quick-filter-${by}">
                <a class="text-light fw-lighter nav-link active">
                        <i class="sort-icon fa-solid fa-arrow-down-wide-short me-1"></i>
                        ${$selected.text()}   
                       <i onclick="clearFilterMutation('${by}')" class="fa-solid fa-circle-minus ms-1"></i>
                </a>
        </li>
        `;
}