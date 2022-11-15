let nameMapper;
let descriptionExpandedMapper;
let autoGetAlarmsInterval;
let alarmServiceUrl;

function clearElement(element) {
    $(element).val("");
}

function updateAlarms() {
    let fromDateStr = $("#fromDate").val();
    let toDateStr = $("#toDate").val();
    let fileIDStr = $("#fileIDFilter").val();
    let component = $("#componentFilter").val();
    let alarmCodeStr = $("#alarmCodeFilter").val();
    let maxAlarmStr = $("#maxAlarms").val();
    let collectionIDStr = $("#collectionIDFilter").val();
    let url = alarmServiceUrl + '/alarm/AlarmService/queryAlarms/';

    getDescriptionExpandedState();

    $.post(url,
        {
            fromDate: fromDateStr,
            toDate: toDateStr,
            fileID: fileIDStr,
            reportingComponent: component,
            alarmCode: alarmCodeStr,
            maxAlarms: maxAlarmStr,
            collectionID: collectionIDStr,
            oldestAlarmFirst: false
        }, function (json) {
            let htmlTableBody = "";
            if (json != null) {
                for (let i = 0; i < json.length; i++) {
                    htmlTableBody += `<tr>
                          <td>${json[i].origDateTime}</td>
                          <td>${json[i].alarmRaiser}</td>
                          <td>${nameMapper.getName(json[i].collectionID)}</td>
                          <td>${json[i].fileID}</td>
                          <td>${json[i].alarmCode}</td>
                          <td class="description">${renderAlarmDescription(i, json[i].alarmText).prop('outerHTML')}</td>
                        </tr>`;
                }
            }
            $("#alarms-table-body").html(htmlTableBody);
            enableDescriptionExpansionOnClick();
        });
}

function getDescriptionExpandedState() {
    descriptionExpandedMapper = {};
    $("#alarms-table-body tr").each(function (index) {
        // Grab the
        descriptionExpandedMapper[index] = $(this).find("td:eq(5)").find(".complete").css("display");
    });
}

function renderAlarmDescription(index, alarmDescription) {
    const maxLength = 80;
    let descriptionHtml = $('<p class="teaser">').css("margin", 0);
    let isExpandedDesc = descriptionExpandedMapper.hasOwnProperty(index) &&
        descriptionExpandedMapper[index] !== "none";

    if (alarmDescription.length <= maxLength || isExpandedDesc) {
        let fullDescSpan = `<span class="complete" style="display: inline">${nl2br(alarmDescription)}</span>`
        descriptionHtml.empty().html(fullDescSpan);
    } else { // Alarm description is too long and is not expanded
        let shortenedDesc = alarmDescription.substring(0, maxLength);
        let remainingDesc = alarmDescription.substring(maxLength, alarmDescription.length);
        descriptionHtml.empty().html(nl2br(shortenedDesc));
        descriptionHtml.append(`<span class="dots">... </span>`)
        descriptionHtml.append(`<span class="complete">${nl2br(remainingDesc)}</span>`);
        descriptionHtml.append('<a href="javascript:void(0);" class="show-more">show more</a>');
    }
    return descriptionHtml;
}

function enableDescriptionExpansionOnClick() {
    $(".teaser").each(function () {
        $(this).children(".show-more").on("click", function () {
            $(this).siblings(".complete").show();
            $(this).siblings(".dots").remove();
            $(this).remove();
        });
    });
}

function init() {
    $.get('repo/urlservice/alarmService/', {}, function (url) {
        alarmServiceUrl = url;
    }, 'html').done(function () {
        $.getJSON('repo/reposervice/getCollections/', {}, function (collections) {
            nameMapper = new CollectionNameMapper(collections);
            let cols = nameMapper.getCollectionIDs();
            for (let i in cols) {
                $("#collectionIDFilter").append(
                    `<option value="${cols[i]}">${nameMapper.getName(cols[i])}</option>`
                );
            }
            updateAlarms();
            autoGetAlarmsInterval = setInterval(function () {
                updateAlarms();
            }, 2500);
        });
    });
}

$(document).ready(function () {
    makeMenu("alarm-service.html", "#pageMenu");
    init();
    $("#fromDate").datepicker({format: "yyyy/mm/dd"});
    $("#toDate").datepicker({format: "yyyy/mm/dd"});
    $("#toDateClearButton").on("click", function (event) {
        event.preventDefault();
        clearElement("#toDate")
    });
    $("#fromDateClearButton").on("click", function (event) {
        event.preventDefault();
        clearElement("#fromDate")
    });
    $("#fileIDClearButton").on("click", function (event) {
        event.preventDefault();
        clearElement("#fileIDFilter")
    });
    $("#componentIDClearButton").on("click", function (event) {
        event.preventDefault();
        clearElement("#componentFilter")
    });
});