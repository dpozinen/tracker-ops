
let global = {
    host : "localhost"
}

window.addEventListener('DOMContentLoaded', event => {

    // Navbar shrink function
    let navbarShrink = function () {
        const navbarCollapsible = document.body.querySelector('#mainNav');
        if (!navbarCollapsible) return;
        if (window.scrollY === 0) {
            navbarCollapsible.classList.remove('navbar-shrink')
        } else {
            navbarCollapsible.classList.add('navbar-shrink')
        }
    };

    // Shrink the navbar
    navbarShrink();

    // Shrink the navbar when page is scrolled
    document.addEventListener('scroll', navbarShrink);

    // Activate Bootstrap scrollspy on the main nav element
    const mainNav = document.body.querySelector('#mainNav');
    if (mainNav) {
        new bootstrap.ScrollSpy(document.body, {
            target: '#mainNav',
            offset: 72,
        });
    }

    // Collapse responsive navbar when toggler is visible
    const navbarToggler = document.body.querySelector('.navbar-toggler');
    const responsiveNavItems = [].slice.call(
        document.querySelectorAll('#navbarResponsive .nav-link')
    );
    responsiveNavItems.map(function (responsiveNavItem) {
        responsiveNavItem.addEventListener('click', () => {
            if (window.getComputedStyle(navbarToggler).display !== 'none') {
                navbarToggler.click();
            }
        });
    });

});

function searchSpinner(enable, border, elem, disabledIcon) {
    if (enable) {
        let spinner = border ?
            `
            <div class="col text-center">
                <div class="spinner-border spinner-border-sm text-danger" role="status">
                    <span class="sr-only">Loading...</span>
                </div>
            </div>
            `
            :
            `
            <div class="col text-center">
                <div class="spinner-grow text-danger" role="status">
                    <span class="sr-only">Loading...</span>
                </div>
            </div>
        `;
        elem.empty().append(spinner);
    } else {
        elem.empty().append(disabledIcon)
    }
}

function search(event) {
    event.preventDefault()
    searchSpinner(true, false, $('#search-divider-icon'))
    let tracker = $('#search-form input[checked]').val();
    let keywords = $('#keywords').val();

    let url = `http://${global.host}:8133/search/${tracker}/${keywords}`;
    $.ajax({
        type: "GET",
        dataType: "json",
        url: url,
        success: function(torrents) {
            $('#torrents').empty()

            if ($.isEmptyObject(torrents)) {
                addNoResultsCard();
            } else {
                $.each(torrents, function(key, value) {
                    addTorrent(value)
                });
            }
            searchSpinner(false, false, $('#search-divider-icon'), '<i class="fa-solid fa-circle-nodes">')
            $("#results")[0].scrollIntoView()
        }
    });
}

function addTorrent(torrent) {
    let card = `
    <div class="col-md" id="torrent-${torrent.index}">
        <div class="card">
            <div class="card-body">
                <h5 class="card-title">${torrent.contributor}</h5>
                <p class="card-text" style="cursor: pointer"
                   onclick="fetchLink(this, '${torrent.link}', '${torrent.index}')">
                    ${torrent.name}
                </p>
                <div class="row">
                    <div class="col-4">
                        <h6 class="card-subtitle mb-2 text-muted t-size">${torrent.size}</h6>
                    </div>
                    <div class="col-4">
                        <h6 style="color:darkseagreen" class="card-subtitle t-seed">${torrent.seeds}</h6>
                    </div>
                    <div class="col-4">
                        <h6 style="color:indianred" class="card-subtitle t-leech">${torrent.leeches}</h6>
                    </div>
                </div>
                <h6 class="card-text t-date"><small class="text-muted">${torrent.date}</small></h6>
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

    $('#torrents').append(card)
}

function addNoResultsCard() {
    let card = `
    <div class="col">
        <div class="card text-center">
            <div class="card-body">
                <h5 class="card-title">No torrents found :(</h5>
            </div>
        </div>
    </div>
    `

    $('#torrents').append(card)
}

function fetchLink(elem, link, cardId) {
    let card = $(`#torrent-${cardId}`)
    showSpinner(card);

    $.ajax({
        type: "GET",
        dataType: "json",
        url: link,
        success: function(torrent) {
            $('.spinner-grow', card).remove()

            $('a', card).attr('href', torrent.link)
            $('a', card).removeAttr('hidden')
            $('button', card).removeAttr('hidden')
            $('.copy-magnet', card).click(function () {
                navigator.clipboard.writeText(torrent.link)
            })
            $('.manual-magnet', card).click(function () {
                searchSpinner(true, true, $('.manual-magnet', card))
                $.ajax({
                    url: `http://${global.host}:8133/deluge`,
                    method: "POST",
                    contentType: "text/plain",
                    data: torrent.link,
                    success: function () {
                        $('.manual-magnet', card).removeClass('btn-outline-danger')
                            .addClass('btn-outline-primary')
                        searchSpinner(false, true, $('.manual-magnet', card),
                    '<i class="fa-solid fa-check"></i> <i class="fa-solid fa-magnet"></i>')
                    },
                    error: function () {
                        $('.manual-magnet', card).removeClass('btn-outline-primary')
                            .addClass('btn-outline-danger')
                        searchSpinner(false, true, $('.manual-magnet', card),
                            `
                                        <i class="fa-solid fa-circle-exclamation"></i>
                                        <i class="fa-solid fa-magnet"></i>
                                        `)
                    }
                })
            })
        }
    });
}

function showSpinner(elem) {
    let hidden = $('a', elem).attr('hidden')
    if (hidden) {
        let spinner = `
        <div class="spinner-grow text-primary" role="status">
            <span class="sr-only">Loading...</span>
        </div>
        `;
        $('.card-body', elem).append(spinner)
    }
}


// todo fix weird cors issues
function sendToMyDeluge(magnet) {
    let addMagnet = function (config) {
        let location = config.result.download_location
        sendToDeluge(
            delugeParams("core.add_torrent_magnet",
                [ magnet, {"download_location": location} ]
            )
        )
    };
    let getConfig = function () {
        sendToDeluge(
            delugeParams("core.get_config", []),
            addMagnet
        )
    };
    let auth = function () {
        sendToDeluge(
            delugeParams("auth.login", ["deluge"]),
            getConfig
        )
    }
    auth();
}

function sendToDeluge(params, onSuccess) {
    $.ajax({
        type: "OPTIONS",
        url: "http://192.168.0.184:8112/json",
        success: function () {
            let request = new XMLHttpRequest();
            request.open("POST", "http://192.168.0.184:8112/json");
            request.setRequestHeader("Content-Type", "application/json")
            request.setRequestHeader("Access-Control-Allow-Origin", "*")
            request.setRequestHeader("Access-Control-Allow-Methods", "POST")
            request.setRequestHeader("Access-Control-Allow-Headers", "Content-Type")
            request.send(JSON.stringify(params));
        },
    })
}

function delugeParams(method, params) {
    return {
        "method": method,
        "params": params,
        "id": 109384
    }
}

// dptodo: separate into several files
// dptodo: add constants
// dptodo: checkboxes for other buttons