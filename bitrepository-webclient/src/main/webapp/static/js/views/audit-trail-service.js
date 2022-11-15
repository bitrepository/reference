const COLLECTION_TABLE_SELECTOR = '#collection-schedule-table-body';
const PRESERVATION_TABLE_SELECTOR = '#preservation-schedule-table-body';
let collectionScheduleUpdater;
let auditTrailServiceUrl;

function clearElement(element) {
    $(element).val("");
}

function startAuditTrailsCollection() {
    let url = auditTrailServiceUrl + '/audittrails/AuditTrailService/collectAuditTrails/';
    $.post(url, {}, function(d) {
        console.log(d);
    });
}

function getAuditTrails() {
    let fromDateStr = $("#fromDate").val();
    let toDateStr = $("#toDate").val();
    let fileIDStr = $("#fileIDFilter").val();
    let component = $("#componentFilter").val();
    let actorStr = $("#actorFilter").val();
    let actionStr = $("#actionFilter").val();
    let collectionIDStr = $("#collectionIDFilter").val();
    let maxAuditTrailsStr = $("#maxAuditTrails").val();
    let fingerprintStr = $("#fingerprintFilter").val();
    let operationIDStr = $("#operationIDFilter").val();

    $("#auditTrailsTableDiv").html("Loading audit trails...");

    let url = auditTrailServiceUrl + '/audittrails/AuditTrailService/queryAuditTrailEvents/';
    $.post(url,
        {fromDate: fromDateStr,
            toDate: toDateStr,
            fileID: fileIDStr,
            reportingComponent: component,
            actor: actorStr,
            action: actionStr,
            collectionID: collectionIDStr,
            maxAuditTrails: maxAuditTrailsStr,
            fingerprint: fingerprintStr,
            operationID: operationIDStr},
        function (j) {
            let htmlTable;
            htmlTable = "<table class=\"table table-bordered table-striped\">";
            htmlTable += "<thead> <tr>";
            htmlTable += "<th style=\"min-width: 100px\">FileID</th>";
            htmlTable += "<th style=\"min-width: 160px\">Component</th>";
            htmlTable += "<th style=\"min-width: 70px\">Actor</th>";
            htmlTable += "<th style=\"min-width: 70px\">Action</th>";
            htmlTable += "<th style=\"min-width: 120px\">Timestamp</th>";
            htmlTable += "<th style=\"min-width: 120px\">Info</th>";
            htmlTable += "<th style=\"min-width: 150px\">Message from client</th>";
            htmlTable += "<th style=\"min-width: 150px\">Certificate fingerprint</th>";
            htmlTable += "<th style=\"min-width: 150px\">OperationID</th>";
            htmlTable += "</tr></thead><tbody>";
            for (let i = 0; i < j.length; i++) {
                htmlTable += "<tr><td>" + j[i].fileID + "</td><td>" + j[i].reportingComponent
                    + "</td><td>" + j[i].actor + "</td><td>" + j[i].action
                    + "</td><td>" + j[i].timeStamp + "</td><td>" + j[i].info
                    + "</td> <td>" + j[i].auditTrailInfo + "</td>"
                    + "<td>" + j[i].fingerprint + "</td>"
                    + "<td>" + j[i].operationID + "</td></tr>";
            }
            htmlTable += "</tbody></table>";
            $("#auditTrailsTableDiv").html(htmlTable);
        }, "json").fail(
        function (jqXHR, textStatus, errorThrown) {
            let htmlElement = "<div class=\"alert alert-error\">";
            htmlElement += "<button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button>";
            htmlElement += "<b>Oh snap!</b> ";
            htmlElement += "Fetching of Audit trails failed with: " + errorThrown + "</div>";
            $("#auditTrailsTableDiv").html(htmlElement);
        });
}

function getCollectionIDs() {
    $.getJSON('repo/reposervice/getCollections/', {}, function (j) {
        for (let i = 0; i < j.length; i++) {
            $("#collectionIDFilter").append('<option value="' + j[i].collectionID + '">' + j[i].collectionName + '</option>');
        }
    });
}

function getContributorIDs() {
    let url = auditTrailServiceUrl + '/audittrails/AuditTrailService/contributors';
    $.getJSON(url, {}, function (ids) {
        let componentFilterElement = $("#componentFilter");
        componentFilterElement.append('<option value=""> ALL </option>');
        for (let i = 0; i < ids.length; i++) {
            componentFilterElement.append('<option value="' + ids[i] + '">' + ids[i] + '</option>');
        }
    });
}

function updateCollectionScheduleRow(schedule) {
    $("#" + schedule.collectionID + "-schedule-laststart").html(schedule.lastStart);
    $("#" + schedule.collectionID + "-schedule-lastduration").html(schedule.lastDuration);
    $("#" + schedule.collectionID + "-schedule-lastaudits").html(schedule.collectedAudits);
    $("#" + schedule.collectionID + "-schedule-nextstart").html(schedule.nextStart);
}

function updateCollectionSchedule() {
    let url = auditTrailServiceUrl + '/audittrails/AuditTrailService/collectionSchedule/';
    $.getJSON(url, {}, function (schedules) {
        for (let i = 0; i < schedules.length; i++) {
            updateCollectionScheduleRow(schedules[i]);
        }
    });
}

function addCollectionScheduleRow(schedule) {
    let html = "<tr>";
    html += "<td>" + schedule.collectionID + "</td>";
    html += "<td id='" + schedule.collectionID + "-schedule-laststart'>" + schedule.lastStart + "</td>";
    html += "<td id='" + schedule.collectionID + "-schedule-lastduration'>" + schedule.lastDuration + "</td>";
    html += "<td id='" + schedule.collectionID + "-schedule-lastaudits'>" + schedule.collectedAudits + "</td>";
    html += "<td id='" + schedule.collectionID + "-schedule-nextstart'>" + schedule.nextStart + "</td>";
    html += "</tr>";
    $(COLLECTION_TABLE_SELECTOR).append(html);
}

function loadCollectionSchedule() {
    let url = auditTrailServiceUrl + '/audittrails/AuditTrailService/collectionSchedule/';
    $.getJSON(url, {}, function (schedules) {
        for (let i = 0; i < schedules.length; i++) {
            addCollectionScheduleRow(schedules[i]);
        }
    }).done(function () {
        collectionScheduleUpdater = setInterval(function () {updateCollectionSchedule(); }, 5000);
    });
}

function loadPreservationSchedule() {
    let url = auditTrailServiceUrl + '/audittrails/AuditTrailService/preservationSchedule/';
    $.getJSON(url, {}, function (preservationInfo) {
        addPreservationScheduleRow(preservationInfo);
    }).done(function () {
        $("#preservation-schedule-accordion").show();
        setInterval(function () {updatePreservationSchedule(); }, 10000);
    }).fail(function () {
        console.log("Preservation is disabled. Not showing preservation schedule.")
    });
}

function addPreservationScheduleRow(preservationInfo) {
    let html = "<tr>";
    html += "<td>" + preservationInfo.collectionID + "</td>";
    html += "<td id='preservation-schedule-laststart'>" + preservationInfo.lastStart + "</td>";
    html += "<td id='preservation-schedule-lastduration'>" + preservationInfo.lastDuration + "</td>";
    html += "<td id='preservation-schedule-lastcount'>" + preservationInfo.preservedAuditCount + "</td>";
    html += "<td id='preservation-schedule-nextstart'>" + preservationInfo.nextStart + "</td>";
    html += "</tr>";
    $(PRESERVATION_TABLE_SELECTOR).append(html);
}

function updatePreservationSchedule() {
    let url = auditTrailServiceUrl + '/audittrails/AuditTrailService/preservationSchedule/';
    $.getJSON(url, {}, function (preservationInfo) {
        $("#preservation-schedule-laststart").html(preservationInfo.lastStart);
        $("#preservation-schedule-lastduration").html(preservationInfo.lastDuration);
        $("#preservation-schedule-lastcount").html(preservationInfo.preservedAuditCount);
        $("#preservation-schedule-nextstart").html(preservationInfo.nextStart);
    });
}

function initPage() {
    $.get('repo/urlservice/auditTrailService', {}, function (url) {
        auditTrailServiceUrl = url;
        let collectAuditTrailsButton = $("#collectAuditTrails");
        let queryAuditTrailsButton = $("#queryAuditTrails");
        collectAuditTrailsButton.on("click", function (event) { event.preventDefault(); startAuditTrailsCollection(); });
        collectAuditTrailsButton.prop("disabled", false);
        queryAuditTrailsButton.on("click", function (event) {event.preventDefault(); getAuditTrails()});
        queryAuditTrailsButton.prop("disabled", false);
        loadCollectionSchedule();
        loadPreservationSchedule();
        getContributorIDs();
    }, 'html');
}

$(document).ready(function () {
    makeMenu("audit-trail-service.html", "#pageMenu");
    initPage();
    getCollectionIDs();
    $("#fromDate").datepicker({format: "yyyy/mm/dd"});
    $("#toDate").datepicker({format: "yyyy/mm/dd"});
    $("#toDateClearButton").on("click", function (event) {event.preventDefault(); clearElement("#toDate")});
    $("#fromDateClearButton").on("click", function (event) {event.preventDefault(); clearElement("#fromDate")});
    $("#fileIDClearButton").on("click", function (event) {event.preventDefault(); clearElement("#fileIDFilter")});
    $("#actorClearButton").on("click", function (event) {event.preventDefault(); clearElement("#actorFilter")});
    $("#fingerprintClearButton").on("click", function (event) {event.preventDefault(); clearElement("#fingerprintFilter")});
    $("#operationIDClearButton").on("click", function (event) {event.preventDefault(); clearElement("#operationIDFilter")});
});
