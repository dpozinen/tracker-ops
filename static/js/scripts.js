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

function search(event) {
    event.preventDefault()
    let tracker = $('#search-form input[checked]').val();
    let keywords = $('#keywords').val();

    let url = `http://192.168.0.130:8133/search/${tracker}/${keywords}`;
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
            $("#results")[0].scrollIntoView()
        }
    });
}

function addTorrent(torrent) {
    let card = `
    <div class="col" id="torrent-${torrent.index}">
        <div class="card" style="cursor: pointer" onclick="fetchLink(this, '${torrent.link}')">
            <div class="card-body">
                <h5 class="card-title">${torrent.contributor}</h5>
                <p class="card-text">${torrent.name}</p>
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

function fetchLink(elem, link) {
    showSpinner(elem);

    $.ajax({
        type: "GET",
        dataType: "json",
        url: link,
        success: function(torrent) {
            $('.spinner-grow', elem).remove()

            $('a', elem).attr('href', torrent.link)
            $('a', elem).removeAttr('hidden')
            $('button', elem).removeAttr('hidden')
            $('button .copy-magnet', elem).click(copyToClipboard(torrent.link))
            $('button .manual-magnet', elem).click(sendToMyDeluge(torrent.link))
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

function copyToClipboard(value) {
    navigator.clipboard.writeText(value);
}

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
        type: "POST",
        dataType: "json",
        url: "http://192.168.0.184:8112/json",
        body: JSON.stringify(params),
        contentType: "application/json; charset=utf-8",
        success: onSuccess
    })
}

function delugeParams(method, params) {
    return {
        "method": method,
        "params": params,
        "id": 109384
    }
}
