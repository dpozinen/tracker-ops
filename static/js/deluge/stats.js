let statsMode = 'UPLOADED'
let axisTicks = {
    UP_SPEED: {
        tickvals: [0, 50_000, 100_000, 200_000, 500_000, 1_000_000, 2_000_000],
        ticktext: [0, '50 KiB/s', '100 KiB/s', '200 KiB/s', '500 KiB/s', '1 MiB/s', '2 MiB/s']
    },
    DOWN_SPEED: {
        tickvals: [0, 50_000, 100_000, 200_000, 500_000, 1_000_000, 2_000_000],
        ticktext: [0, '50 KiB/s', '100 KiB/s', '200 KiB/s', '500 KiB/s', '1 MiB/s', '2 MiB/s']
    },
    UPLOADED: {
        tickvals: [500_000_000, 1_000_000_000, 20_000_000_000, 80_000_000_000, 100_000_000_000, 300_000_000_000, 500_000_000_000, 1_000_000_000_000],
        ticktext: ['500 MiB',   '1 GiB',        '20 GiB',       '80 GiB',       '100 GiB',      '300 GiB',       '500 GiB',       '1 TiB']
    }
}

window.addEventListener('mutated', function (e) {
    requestStats()
}, false);



function initStats() {
    openSocket();
    $('[data-toggle="tooltip"]').tooltip()
    stomp = Stomp.over(sock);
    stomp.connect({},
        function () {
            console.log("Connected")
            stomp.send("/stream/commence");
            receiving = true
            stomp.subscribe('/topic/torrents', function(data) {
                let msg = JSON.parse(data.body);
                renderInfo(msg.info)
            });
        })
    requestStats()
}

function requestStats() {
    let url = `http://${global.host}:8133/deluge/stats?ago=3d&interval=5h&minPoints=10&fillEnd=true`;
    $.ajax({
        type: "GET",
        dataType: "json",
        url: url,
        success: function(data) {
            chart(data)
        }
    });
}

function chart(data) {
    function extractStat(val) {
        return statsMode === "DOWN_SPEED" ? val.downSpeed :
            statsMode === "UP_SPEED" ? val.upSpeed :
                val.uploaded
    }

    let datasets = $.map(data.torrents, function (val, i) {
        let stat = data.stats[val.id]
        return {
            line: {
                shape: 'spline',
                smoothing: 1.2
            },
            name: val.name,
            mode: 'lines+markers',
            y: $.map(stat, function (val, i) {

                let empty = statsMode !== "UPLOADED" && val.id === -1
                return empty ? null : extractStat(val)
            }),
            x: $.map(stat, function (val, i) { return val.time })
        }
    });

    datasets.sort(function(a, b) {
        return b.y.reduce((a, b) => a + b, 0) - a.y.reduce((a, b) => a + b, 0)
    });

    let layout = {
        hovermode: false,
        autosize: true,
        // width: 1700,
        height: 800,
        yaxis: {
            tickmode: "array",
            tickvals: axisTicks[statsMode].tickvals,
            ticktext: axisTicks[statsMode].ticktext
        }
    }

    Plotly.newPlot('chart', datasets, layout);

    $('#chart').on('plotly_restyle', function(){
        $("#chart")[0].scrollIntoView()
    });
}