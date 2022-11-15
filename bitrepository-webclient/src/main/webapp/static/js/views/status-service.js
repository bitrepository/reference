let knownComponents = new Set();
let monitoringServiceUrl;
let update_component_status;

function makeComponentRow(id, status, time) {
    let html = "";
    let classAttr = "class=\"success\"";
    if (status === "WARNING") {
        classAttr = "class=\"alert\"";
    } else if (status === "UNRESPONSIVE" || status === "ERROR") {
        classAttr = "class=\"alert alert-error\"";
    } else if (status === "UNKNOWN") {
        classAttr = "class=\"info\"";
    }
    html += "<tr " + classAttr + " id=\"" + id +"-row\">";
    html += "<td>" + id + "</td>";
    html += "<td id=\"" + id + "-status\">" + status + " </td>";
    html += "<td id=\"" + id + "-time\">" + time + "</td>";
    html += "<td> <button class=\"btn btn-mini\" id=\"" + id + "-msg-btn\"> Show details <i class=\"icon-chevron-right\"></i></button></td>";
    html += "<tr>";
    return html;
}

function updateComponentRow(id, status, time) {
    let rowElement = $("#" + id + "-row");
    rowElement.removeClass("success alert alert-error info");
    let newClass = "success";
    if (status === "WARNING") {
        newClass = "alert";
    } else if (status === "UNRESPONSIVE" || status === "ERROR") {
        newClass = "alert alert-error";
    } else if (status === "UNKNOWN") {
        newClass = "info";
    }
    rowElement.addClass(newClass);
    $("#" + id + "-status").html(status);
    $("#" + id + "-time").html(time);
}

function populateStatusServiceConfiguration() {
    let url = monitoringServiceUrl + '/monitoring/MonitoringService/getMonitoringConfiguration/';
    $.getJSON(url, {}, function (json) {
        let htmlTableBody = "";
        for (let i = 0; i < json.length; i++) {
            htmlTableBody += "<tr><td>" + json[i].confOption + "</td><td>" + json[i].confValue + "</td></tr>";
        }
        $("#configuration-table-body").html(htmlTableBody);
    });
}

function attachButtonAction(id, message) {
    let element = "#" + id + "-msg-btn";
    $(element).popover({
        placement : "right",
        html: true,
        title: id + " status message",
        content: message
    });
}

function getStatuses() {
    let url = monitoringServiceUrl + '/monitoring/MonitoringService/getComponentStatus/';
    $.getJSON(url, {}, function (json) {
        for (let i = 0; i < json.length; i++) {
            let componentID = json[i].componentID;
            if (knownComponents.has(componentID)) {
                updateComponentRow(componentID, json[i].status, json[i].timeStamp);
            } else {
                $("#component-status-table-body").append(
                    makeComponentRow(componentID, json[i].status, json[i].timeStamp));
                attachButtonAction(componentID, json[i].info);
            }
            knownComponents.add(componentID);
        }
    });
}

function initPage() {
    $.get('repo/urlservice/monitoringService', {}, function (url) {
        monitoringServiceUrl = url;
        populateStatusServiceConfiguration();
        getStatuses();
        update_component_status = setInterval(function () { getStatuses(); }, 2500);
    }, 'html');
}

$(document).ready(function () {
    makeMenu("status-service.html", "#pageMenu");
    initPage();
    closePopoverOnClick();
});
