/**
 * Appends the information from the pillar details JSON as rows to a table with the given ID.
 * @param pillarDetails JSON containing the pillar details to build the rows from
 * @param tableBodyId ID of the table to append the rows to
 */
function fillInPillarDetailsTable(pillarDetails, tableBodyId) {
    pillarDetails.forEach((detail) => {
        $(tableBodyId).append(makePillarDetailsRow(detail));
    });
}

/**
 * Make a row in the pillar details table given the pillar details JSON.
 * @param details Pillar details from ReferenceSettings-file as JSON
 * @returns {string} HTML table row containing the pillar details
 */
function makePillarDetailsRow(details) {
    let html = "";
    html += "<tr>";
    html += "<td>" + details.pillarID + "</td>";
    html += "<td>" + details.pillarName + "</td>";
    html += "<td>" + details.pillarType + "</td>";
    html += "<td>" + details.pillarDeleteFileApprover + "</td>";
    html += "</tr>";
    return html;
}

