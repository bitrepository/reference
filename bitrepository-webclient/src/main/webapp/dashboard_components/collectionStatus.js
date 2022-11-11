let collections = {};
let readyForRefresh = false;
let integrityServiceUrl;
let myNameMapper;
let updatePage;
let updateLock;

function setIntegrityServiceUrl(url) {
    integrityServiceUrl = url;
}

function setNameMapper(nameMapper) {
    myNameMapper = nameMapper;
}

function initiateCollectionStatus(collectionIDs, tableBody, updateInterval) {
    for (let i = 0; i < collectionIDs.length; i++) {
        collections[collectionIDs[i].collectionID] = {collectionID: collectionIDs[i].collectionID,
            collectionName: collectionIDs[i].collectionName,
            numFiles: '-',
            latestIngest: "Fetching",
            collectionSize: '-',
            pillars: '-',
            lastCheck: "Fetching",
            numChecksumErrors: '-',
            numMissingFiles: '-',
            nextCheck: "Fetching"};
        $(tableBody).append(makeCollectionRow(collections[collectionIDs[i].collectionID]));
    }
    readyForRefresh = true;
    initStatusUpdate(updateInterval);
}

function updateWorkflowStatus(collection) {
    if (scheduleUpdate(collection, "status")) {
        let url = integrityServiceUrl + "/integrity/IntegrityService/getWorkflowSetup/?collectionID=" + collection;
        let c = collection;
        $.getJSON(url, {}, function(j) {
            collections[c].lastCheck = j[0].lastRun;
            collections[c].nextCheck = j[0].nextRun;
        }).done(function(){updateCollectionRow(collections[c]);}).always(function() {finishUpdate(c, "status");});
    }
}

function updateWorkflowStatuses() {
    if (readyForRefresh) {
        for (let c in collections) {
            updateWorkflowStatus(c);
        }
    }
}

function updateCollectionInfo(collection) {
    if (scheduleUpdate(collection, "info")) {
        let url = integrityServiceUrl + "/integrity/IntegrityService/getCollectionInformation/?collectionID=" + collection;
        $.getJSON(url, {}, function(j) {
            collections[collection].numFiles = j.numberOfFiles;
            collections[collection].collectionSize = j.collectionSize;
            collections[collection].latestIngest = j.lastIngest;
        }).done(function(){updateCollectionRow(collections[c]);}).always(function() {finishUpdate(c, "info");});
    }
}

function updateInfo() {
    if (readyForRefresh) {
        for (let c in collections) {
            updateCollectionInfo(c);
        }
    }
}

function updateCollectionStatistic(collection) {
    if (scheduleUpdate(collection, "stats")) {
        let url = integrityServiceUrl + "/integrity/IntegrityService/getIntegrityStatus/?collectionID=" + collection;
        $.getJSON(url, {}, function(j) {
            let checksumErrors = 0;
            let missingFiles = 0;
            let pillarCount = 0;
            for (let stat in j) {
                pillarCount += 1;
                checksumErrors += j[stat].checksumErrorCount;
                missingFiles += j[stat].missingFilesCount;
            }
            collections[collection].pillars = pillarCount;
            collections[collection].numChecksumErrors = checksumErrors;
            collections[collection].numMissingFiles = missingFiles;
        }).done(function(){updateCollectionRow(collections[c])}).always(function() {finishUpdate(c, "stats");});
    }
}

function updateStatistics() {
    if (readyForRefresh) {
        for (let c in collections) {
            updateCollectionStatistic(c);
        }
    }
}

function makeCollectionRow(collection) {
    let id = collection.collectionID;
    let html = "";
    html += "<tr id=\"" + id + "-row\">";
    html += "<td id=\""+ id + "-name\" class=\"collectionName\">" + id + "</div></td>";
    html += "<td id=\""+ id + "-numFiles\"></td>";
    html += "<td id=\""+ id + "-latestIngest\"></td>";
    html += "<td id=\""+ id + "-collectionSize\"></td>";
    html += "<td id=\""+ id + "-pillars\"></td>";
    html += "<td id=\""+ id + "-latestCheck\"></td>";
    html += "<td> <span id=\""+ id + "-numChecksumErrors\"></span></td>";
    html += "<td> <span id=\""+ id + "-numMissingFiles\"></span></td>";
    html += "<td id=\""+ id + "-nextCheck\"></td></tr>";
    return html;
}

function updateCollectionRow(collection) {
    let id = collection.collectionID;
    $("#" + id + "-name").html(`<a href='integrity-service.html?collectionID=${collection.collectionID}'>${collection.collectionName}</a>`);
    $("#" + id + "-numFiles").html(numeral(collection.numFiles).format('0,0'));
    $("#" + id + "-latestIngest").html(collection.latestIngest);
    $("#" + id + "-collectionSize").html(collection.collectionSize);
    $("#" + id + "-pillars").html(collection.pillars);
    $("#" + id + "-latestCheck").html(collection.lastCheck);
    let checksumErrorsNumberElement = $("#" + id + "-numChecksumErrors");
    checksumErrorsNumberElement.html(numeral(collection.numChecksumErrors).format('0,0'));
    if (collection.numChecksumErrors > 0) {
        checksumErrorsNumberElement.addClass("badge badge-important pull-right");
    } else {
        checksumErrorsNumberElement.removeClass("badge badge-important pull-right");
    }
    let missingFilesNumberElement = $("#" + id + "-numMissingFiles");
    missingFilesNumberElement.html(numeral(collection.numMissingFiles).format('0,0'));
    if (collection.numMissingFiles > 0) {
        missingFilesNumberElement.addClass("badge badge-important pull-right");
    } else {
        missingFilesNumberElement.removeClass("badge badge-important pull-right");
    }
    $("#" + id + "-nextCheck").html(collection.nextCheck);
}

function refreshContent() {
    updateWorkflowStatuses();
    updateInfo();
    updateStatistics();
}

function scheduleUpdate(collection, type) {
    if (updateLock[collection][type]) {
        updateLock[collection][type] = false;
        return true;
    } else {
        return false;
    }
}

function finishUpdate(collection, type) {
    return updateLock[collection][type] = true;
}

function initStatusUpdate(updateInterval) {
    // Init update locks
    updateLock = [];
    for (let c in collections) {
        let row = [];
        row["status"] = true;
        row["info"] = true;
        row["stats"] = true;
        updateLock[c] = row;
    }
    refreshContent();
    updatePage = setInterval(function() { refreshContent(); }, updateInterval);
}



