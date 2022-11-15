let pillar_size_data;
let pillar_size_table_data;

function drawPillarDataSizePieChart(url) {
    //Load data, when done, draw chart. 
    $.getJSON(url, {}, function(j) {
        pillar_size_data = [];
        pillar_size_table_data = [];
        for (let i = 0; i < j.length; i++) {
            pillar_size_data[i] = {label: j[i].pillarID, data: j[i].dataSize};
            pillar_size_table_data[i] = {pillar: j[i].pillarID, size: j[i].humanSize};
        }
    }).done(function() {
        let options = { series: {
                pie: {
                    show: true,
                    radius: 1,
                    label: {
                        radius: 3/5,
                        formatter: function (label, series) {
                            return '<div style="font-size:.85em;text-align:center;padding:5px;color:white;">' + label + '<br/>' + Math.round(series.percent) + '%</div>';
                        },
                        background: {
                            opacity: 0.6,
                            color: '#000'
                        }
                    }
                }
            },
            legend: {show: false}
        };
        $.plot($("#data_pillar #flotcontainer_data_pillar"), pillar_size_data, options);

        // Make legend
        let legendHtml = "<table>";
        for(let i = 0; i < pillar_size_table_data.length; i++) {
            legendHtml += "<tr><td class=\"dataLabel\">" + pillar_size_table_data[i].pillar + "</td><td class=\"dataData\">" + pillar_size_table_data[i].size + "</td></tr>";
        }
        legendHtml += "</table>";
        $("#pillarLegendDiv").html(legendHtml);
    });
}
