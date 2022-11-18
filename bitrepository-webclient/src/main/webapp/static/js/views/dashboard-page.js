let update_data_size_graph;
let colorMapper;
let nameMapper;
let dsGraph;

function init() {
    $.get('repo/urlservice/integrityService', {}, function (url) {
        setIntegrityServiceUrl(url);
        $.get("repo/reposervice/getRepositoryName/", {}, function (j) {
            $("#pageHeader").html("Overview of " + j);
        }, "html");
        $.getJSON("repo/reposervice/getCollections/", {}, function (collections) {
            colorMapper = new ColorMapper(collections);
            nameMapper = new CollectionNameMapper(collections);
            setNameMapper(nameMapper);
            initiateCollectionStatus(collections, "#collectionStatusBody", 10000);
            let dataUrl = url + "/integrity/Statistics/getDataSizeHistory/?collectionID=";
            dsGraph = new DataSizeGraph(collections, colorMapper, new FileSizeUtils(), dataUrl, "#graphType", "#dataSizeGraphPlaceholder");
            makeCollectionSelectionCheckboxes("#dataSizeGraphCollectionSelection", dsGraph, colorMapper, nameMapper);
            drawPillarDataSizePieChart(url + "/integrity/Statistics/getLatestPillarDataSize/");
            drawCollectionDataSizePieChart(url + "/integrity/Statistics/getLatestcollectionDataSize/", colorMapper);
            $("#graphType").on("change", function (event) {
                event.preventDefault(); dsGraph.graphTypeChanged();
            });
            dsGraph.updateData();
            // Update graphs every hour
            update_data_size_graph = setInterval(function () {
                dsGraph.updateData();
                drawPillarDataSizePieChart(url + "/integrity/Statistics/getLatestPillarDataSize/");
                drawCollectionDataSizePieChart(url + "/integrity/Statistics/getLatestcollectionDataSize/", colorMapper);
            }, 3600000);
        });
    }, 'html');
}

$.ajaxSetup({cache: false});
$(document).ready(function () {
    makeMenu("dashboard.html", "#pageMenu");
    init();
});