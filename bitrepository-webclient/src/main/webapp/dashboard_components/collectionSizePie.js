let collectionSizeData;
let collectionSizeTableData;

function drawCollectionDataSizePieChart(url, colorMapper) {
    //Load data, when done, draw chart.
    $.getJSON(url, {}, function(j) {
        collectionSizeData = [];
        collectionSizeTableData = [];
        for (let i = 0; i < j.length; i++) {
            collectionSizeData[i] = {label: j[i].collectionName, data: j[i].dataSize, color: colorMapper.getCollectionColor(j[i].collectionID)};
            collectionSizeTableData[i] = {collection: j[i].collectionName, size: j[i].humanSize};
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
        $.plot($("#collection #flotcontainer_collection"), collectionSizeData, options);

        // Make legend
        let legendHtml = "<table>";
        for(let i = 0; i < collectionSizeTableData.length; i++) {
            legendHtml += "<tr><td class=\"dataLabel\">" + collectionSizeTableData[i].collection + "</td><td class=\"dataData\">" + collectionSizeTableData[i].size + "</td></tr>";
        }
        legendHtml += "</table>";
        $("#collectionLegendDiv").html(legendHtml);
    });
}

