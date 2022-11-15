function displayCollectionsListing(collections) {
    let html = "<h3>Collections:</h3>"

    for (let i = 0; i < collections.length; i++) {
        let collection = collections[i];
        html += "<pre><dl>";
        html += "<dt><b>" + collection.collectionName + "</b> <small><i>(ID: " + collection.collectionID + ")</i></small></dt>";
        for (let j = 0; j < collection.pillars.length; j++) {
            html += "<dd>" + collection.pillars[j] + "</dd>";
        }
        html += "</dl></pre>";
    }

    $("#collectionsListingsDiv").append(html);
}

function displayProtocolSettings(settings) {
    let html = "<h3>Protocol settings</h3>";

    html += "<table class=\"table table-hover table-condensed table-bordered\">";
    html += "<thead><tr>";
    html += "<th>Settings key</th>";
    html += "<th>Value</th>";
    html += "</tr></thead>";
    html += "<tbody>"
    for (let s in settings) {
        html += "<tr><td>" + s + "</td>";
        html += "<td>" + settings[s] + "</td><tr>";
    }
    html += "</tbody></table>";

    $("#protocolSettingsDiv").append(html);

}

function getConfiguration() {
    $.getJSON('repo/reposervice/getConfigurationOverview/', {}, function (json) {
        $("#headlineDiv").html("<h2>Configuration for " + json["repositoryName"] + "</h2>");
        displayCollectionsListing(json["collections"]);
        displayProtocolSettings(json["protocolSettings"]);
    });
}

$(document).ready(function () {
    makeMenu("configuration.html", "#pageMenu");
    getConfiguration();
});