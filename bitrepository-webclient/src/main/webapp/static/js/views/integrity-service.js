let pillarIntegrityStatuses = {};
let workflows = {};
let modal;
let downloadModal;
let updatePageInterval;
let nameMapper;
let integrityServiceUrl;

function formatInt(number) {
    return numeral(number).format('0,0');
}

function loadWorkflows() {
    let url = integrityServiceUrl + '/integrity/IntegrityService/getWorkflowList/?collectionID=' + getCollectionID();
    $.getJSON(url, {}, function (json) {
        for (let i = 0; i < json.length; i++) {
            $("#workflowSelector").append('<option value="' + json[i] + '">' + json[i] + '</option>');
        }
    });
}

function makeWorkflowRow(workflowID, nextRun, lastRun, executionInterval, currentState, lastRunState) {
    let html = "";
    html += "<tr><td><a class=\"btn btn-link\" id=\"" + workflowID + "-details\">" + workflowID + "</a></td>";
    html += "<td><div id=\"" + workflowID + "-nextRun\" style=\"padding:5px\">" + nextRun + "</div></td>";
    html += "<td><a class=\"btn btn-link\" id=\"" + workflowID + "-lastRun\">" + lastRun
        + "</a> <span id=\"" + workflowID + "-lastRunState\">(" + lastRunState + ")</span></td>";
    html += "<td><div id=\"" + workflowID + "-executionInterval\" style=\"padding:5px\">" + executionInterval + "</div></td>";
    html += "<td><div id=\"" + workflowID + "-currentState\" style=\"padding:5px\">" + currentState + "</div></td></tr>";
    return html;
}

function updateWorkflowRow(workflowID, nextRun, lastRun, executionInterval, currentState, lastRunState) {
    $("#" + workflowID + "-nextRun").html(nextRun);
    $("#" + workflowID + "-lastRun").html(lastRun);
    $("#" + workflowID + "-lastRunState").html("(" + lastRunState + ")");
    $("#" + workflowID + "-executionInterval").html(executionInterval);
    $("#" + workflowID + "-currentState").html(currentState);
}

function attachWorkflowInfoButton(id, type) {
    let element;
    let title;
    if (type === "workflowDescription") {
        element = "#" + id + "-details";
        title = "Workflow description";
    } else if (type === "lastRunDetails") {
        element = "#" + id + "-lastRun";
        title = "Last run details";
    }
    if (element != null) {
        $(element).popover({
            placement: "right",
            title: title,
            html: true,
            content: getStoredWorkflowInfo(id, type)
        });
    }
}

function getStoredWorkflowInfo(id, type) {
    return function () {
        return nl2br(workflows[id][type]);
    }
}

function getWorkflowStatuses() {
    let url = integrityServiceUrl + '/integrity/IntegrityService/getWorkflowSetup?collectionID=' + getCollectionID();
    $.getJSON(url, {}, function (json) {
        for (let i = 0; i < json.length; i++) {
            if (workflows[json[i].workflowID] == null) {
                $("#workflow-status-table-body").append(
                    makeWorkflowRow(json[i].workflowID, json[i].nextRun, json[i].lastRun, json[i].executionInterval,
                        json[i].currentState, json[i].lastRunFinishState));
                workflows[json[i].workflowID] = {
                    workflowDescription: json[i].workflowDescription,
                    lastRunDetails: json[i].lastRunDetails
                };
                attachWorkflowInfoButton(json[i].workflowID, "workflowDescription");
                attachWorkflowInfoButton(json[i].workflowID, "lastRunDetails");
            } else {
                updateWorkflowRow(json[i].workflowID, json[i].nextRun, json[i].lastRun, json[i].executionInterval,
                    json[i].currentState, json[i].lastRunFinishState);
                workflows[json[i].workflowID].workflowDescription = json[i].workflowDescription;
                workflows[json[i].workflowID].lastRunDetails = json[i].lastRunDetails;
            }
        }
    });
}

function startWorkflow() {
    let ID = $("#workflowSelector option:selected").val();
    let url = integrityServiceUrl + '/integrity/IntegrityService/startWorkflow';
    $('#formStatus').load(url, {
        workflowID: ID,
        collectionID: getCollectionID()
    }).show().fadeOut({duration: 5000});
}

/**
 * Callback method to get the value of a specific property from a pillar’s integrity status.
 * The callback is intended for use by {@see TableModal}.
 *
 * NOTE: The reason for returning a callback instead of the property directly is because of the modal's nature of
 * paging using the integrity status API. As results from the API can change from one call to the next this ensures
 * that the modal page-count properly reflects the underlying state instead of just showing a once-passed static
 * value.
 */
function getIntegrityStatusPropertyCountCallback(pillarID, propertyName) {
    return function () {
        return pillarIntegrityStatuses[pillarID][propertyName];
    }
}

function showModal(type, propertyName, title, url, pillarID) {
    return function () {
        modal = new TableModal(type, pillarID, url, "#modalBody",
            getIntegrityStatusPropertyCountCallback(pillarID, propertyName), 100);
        $("#modalLabel").html(title);
        $("#modalBody").html("<p>Loading</p>");
        modal.getModal(1);
        $("#modalDialog").modal('show');
    }
}

function showDownloadModal(integrityURL) {
    return function () {
        downloadModal = new DownloadModal(getCollectionID(), "#modalBody", integrityURL);
        $("#modalLabel").html("Select Reports to Download");
        $("#modalBody").html("<p>Loading</p>");
        downloadModal.getModal();
        $("#modalDialog").modal('show');
    }
}

function getBodyContext(id, type) {
    let context = {};
    context.url = integrityServiceUrl + "/integrity/IntegrityService/";

    if (type === "Pillar Name") {
        context.element = id + "-pillarName";
        context.title = type + " on " + id.toUpperCase();
        context.propertyName = "pillarName";
    } else if (type === "Pillar Type") {
        context.element = id + "-pillarType";
        context.title = type + " on " + id.toUpperCase();
        context.propertyName = "pillarType";
    } else if (type === "Total files") {
        context.element = id + "-totalFileCount";
        context.title = type + " on " + id.toUpperCase();
        context.propertyName = "totalFileCount";
        context.url += "getTotalFileIDs";
    } else if (type === "Missing files") {
        context.element = id + "-missingFiles";
        context.title = type + " on " + id.toUpperCase();
        context.propertyName = "missingFilesCount";
        context.url += "getMissingFileIDs";
    } else if (type === "Missing checksums") {
        context.element = id + "-missingChecksums";
        context.title = type + " on " + id.toUpperCase();
        context.propertyName = "missingChecksumsCount";
        context.url += "getMissingChecksumsFileIDs";
    } else if (type === "Obsolete checksums") {
        context.element = id + "-obsoleteChecksums";
        context.title = type + " on " + id.toUpperCase();
        context.propertyName = "obsoleteChecksumsCount";
        context.url += "getObsoleteChecksumsFileIDs";
    } else if (type === "Inconsistent checksums") {
        context.element = id + "-checksumErrors";
        context.title = type + " on " + id.toUpperCase();
        context.propertyName = "checksumErrorCount";
        context.url += "getChecksumErrorFileIDs";
    } else if(type === "Checksum age limit") {
        context.element = id + "-maxAgeForChecksums";
    } else if(type === "Oldest checksum age") {
        context.element = id + "-ageOfOldestChecksum";
    }
    context.url += "?pillarID=" + id + "&collectionID=" + getCollectionID();
    return context;
}

function getHeaderContext(type) {
    let context = {};
    if (type === "Number of missing files") {
        context.element = "all-missingFiles";
        context.title = type + " on all pillars";
    } else if (type === "Number of missing checksums") {
        context.element = "all-missingChecksums";
        context.title = type + " on all pillars";
    } else if (type === "Number of obsolete checksums") {
        context.element = "all-obsoleteChecksums";
        context.title = type + " on all pillars";
    } else if (type === "Number of inconsistent checksums") {
        context.element = "all-checksumErrors";
        context.title = type + " on all pillars";
    }
    return context;
}

function makePillarRow(id) {
    let html = "";
    html += "<tr id=\"" + id + "-row\">";
    html += "<td><p class='pillar-info' '>" + id + "</p></td>";
    html += "<td id=\"" + id + "-pillarName\"></td>";
    html += "<td id=\"" + id + "-pillarType\"></td>";
    html += "<td id=\"" + id + "-totalFileCount\"></td>";
    html += "<td id=\"" + id + "-missingFiles\"></td>";
    html += "<td id=\"" + id + "-missingChecksums\"></td>";
    html += "<td id=\"" + id + "-obsoleteChecksums\"></td>";
    html += "<td id=\"" + id + "-checksumErrors\"></td>";
    html += "<td id=\"" + id + "-maxAgeForChecksums\"></td>";
    html += "<td id=\"" + id + "-ageOfOldestChecksum\"></td>";
    html += "</tr>";
    return html;
}

function updateTableHeader() {
    setHeader("Number of missing files");
    setHeader("Number of missing checksums");
    setHeader("Number of obsolete checksums");
    setHeader("Number of inconsistent checksums");
    setHeader("Configured max age of checksums");
    setHeader("Age of oldest checksum");
}

function setHeader(type) {
    let context = getHeaderContext(type);
    let html = `${type}`;
    $("#" + context.element).html(html);
}

function updateTableBody(pillarID) {
    updateStringCell(pillarID, "Pillar Name", pillarIntegrityStatuses[pillarID].pillarName);
    updateStringCell(pillarID, "Pillar Type", pillarIntegrityStatuses[pillarID].pillarType);
    updateIntCell(pillarID, "Total files", pillarIntegrityStatuses[pillarID].totalFileCount);
    updateIntCell(pillarID, "Missing files", pillarIntegrityStatuses[pillarID].missingFilesCount);
    updateIntCell(pillarID, "Missing checksums", pillarIntegrityStatuses[pillarID].missingChecksumsCount);
    updateIntCell(pillarID, "Obsolete checksums", pillarIntegrityStatuses[pillarID].obsoleteChecksumsCount);
    updateIntCell(pillarID, "Inconsistent checksums", pillarIntegrityStatuses[pillarID].checksumErrorCount);
    updateStringCell(pillarID, "Checksum age limit", pillarIntegrityStatuses[pillarID].maxAgeForChecksums);
    updateStringCell(pillarID, "Oldest checksum age", pillarIntegrityStatuses[pillarID].ageOfOldestChecksum);
}

function updateStringCell(id, type, cellValue) {
    let context = getBodyContext(id, type);
    let html = "<p class='pillar-info'>" + cellValue + "</p>";
    $("#" + context.element).html(html);
}

function updateIntCell(pillarID, type, cellValue) {
    let context = getBodyContext(pillarID, type);

    if (cellValue === 0) {
        let html = `<button class="btn btn-link" disabled>${formatInt(cellValue)}</button>`;
        $("#" + context.element).html(html);
    } else {
        let innerElement = context.element + "-a";
        let html = `<a class="btn btn-link" id="${innerElement}">${formatInt(cellValue)}</a>`;
        $("#" + context.element).html(html);
        $("#" + innerElement).on("click", showModal(type, context.propertyName, context.title, context.url, pillarID));
    }
}

function initializePage() {
    $.get('repo/urlservice/integrityService/', {}, function (url) {
        integrityServiceUrl = url;
    }, 'html').done(function () {
        $.getJSON('repo/reposervice/getCollections/', {}, function (collections) {
            nameMapper = new CollectionNameMapper(collections);
            let cols = nameMapper.getCollectionIDs();
            for (let i in cols) {
                $("#collectionChooser").append(
                    `<option value="${cols[i]}">${nameMapper.getName(cols[i])}</option>`);
            }
            let collectionID = new URLSearchParams(window.location.search).get("collectionID");
            if (collectionID !== null && cols.includes(collectionID)) {
                setCollection(collectionID);
            } else {
                collectionChanged(getCollectionID());
            }
        });
    });
}

function collectionChanged(collectionID) {
    clearContent();
    $("#integrityLegend").html("Integrity information for collection " + nameMapper.getName(collectionID));
    let reportUrl = `${integrityServiceUrl}/integrity/IntegrityService`;
    $("#integrityReportGetter").html(`<a class="btn btn-link" target="_blank">Download latest integrity reports</a>`).on("click",
        showDownloadModal(reportUrl));

    clearInterval(updatePageInterval);
    loadWorkflows();
    getCollectionInformation(collectionID);
    getWorkflowStatuses();
    getIntegrityStatus();
    updatePageInterval = setInterval(function () {
        getWorkflowStatuses();
        getIntegrityStatus();
        getCollectionInformation(collectionID);
    }, 2500);
}

function setCollection(collectionID) {
    $("#collectionChooser").val(collectionID).on("change", );
}

function getIntegrityStatus() {
    let url = integrityServiceUrl + "/integrity/IntegrityService/getIntegrityStatus?collectionID="
        + getCollectionID();
    $.getJSON(url, {}, function (json) {
        for (let i = 0; i < json.length; i++) {
            if (pillarIntegrityStatuses[json[i].pillarID] == null) {
                let tableBody = $("#integrity-status-table-body");
                tableBody.append(makePillarRow(json[i].pillarID));
            }
            pillarIntegrityStatuses[json[i].pillarID] = {
                pillarName: json[i].pillarName,
                pillarType: json[i].pillarType,
                totalFileCount: json[i].totalFileCount,
                missingFilesCount: json[i].missingFilesCount,
                missingChecksumsCount: json[i].missingChecksumsCount,
                obsoleteChecksumsCount: json[i].obsoleteChecksumsCount,
                checksumErrorCount: json[i].checksumErrorCount,
                maxAgeForChecksums: json[i].maxAgeForChecksums,
                ageOfOldestChecksum: json[i].ageOfOldestChecksum
            };
            updateTableBody(json[i].pillarID);
            updateTableHeader();
        }
    });
}

function getCollectionID() {
    return $("#collectionChooser").val();
}

function getCollectionInformation() {
    let url = integrityServiceUrl + "/integrity/IntegrityService/getCollectionInformation?collectionID=" + getCollectionID();
    $.getJSON(url, {}, function (json) {
        let infoHtml = `<span><b>Latest ingest: ${json.lastIngest} &nbsp; &nbsp;
                Size: ${json.collectionSize} &nbsp; &nbsp;
                Number of files: ${formatInt(json.numberOfFiles)}</b></span>`;

        $("#collectionInfoDiv").html(infoHtml);
    });

}

function clearContent() {
    $("#integrity-status-table-body").empty();
    $("#workflow-status-table-body").empty();
    $("#workflowSelector").empty();
    $("#collectionInfoDiv").empty();
    $("#integrityReportGetter").empty();
    pillarIntegrityStatuses = {};
    workflows = {};
}

$(document).ready(function () {
    // Load page content
    makeMenu("integrity-service.html", "#pageMenu");
    initializePage();

    // Setup event / click handling
    $("#workflowStarter").on("click", function (event) {
        event.preventDefault();
        startWorkflow();
    });
    $("#collectionChooser").on("change", function (event) {
        event.preventDefault();
        collectionChanged(getCollectionID());
    });

    closePopoverOnClick();
});