/*
 * #%L
 * Bitrepository Webclient
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */

function DownloadModal(collectionID, contentElement, url) {
    this.url = url;
    this.collectionID = collectionID;

    this.getModal = function () {
        let self = this;
        $.getJSON(`${self.url}/getAvailableIntegrityReports?collectionID=${self.collectionID}`, {}, function (json) {
            let html = `<p style="text-align: center; margin: 0;">`;
            html += `Select the reports you wish to download. If none are selected, only the latest full integrity report will be downloaded.`;
            html += `</p>`;

            // Create "select all" checkbox and "reload table" button.
            html += `<hr><div style="text-align: center;">`;
            html += `Select all: <input type="checkbox" name="select-all" style="margin-bottom: 5px;"/>`;
            html += `<span id="refresh" title="Reload reports" style="cursor: pointer; margin-left: 15px;">&#128260;</span>`;
            html += `</div><hr>`;

            // Create table
            html += `<table class="modal-table" style="width: 100%; border-collapse: separate;">`;

            // Populate the header of the table.
            html += `<thead class="modal-table-head">`;
            html += `<tr style="padding: 5px; border-bottom: 1pt solid black;">`;
            html += `<th style="text-align: left; border-left: 1px solid white; border-right: 1px solid #9996;">Pillar ID</th>`;
            html += `<th style="border-right: 1px solid #9996;">Missing Files</th>`;
            html += `<th style="border-right: 1px solid #9996;">Missing Checksums</th>`;
            html += `<th style="border-right: 1px solid #9996;">Obsolete Checksums</th>`;
            html += `<th style="border-right: 1px solid #9996;">Inconsistent Checksums</th>`;
            html += `<th>Deleted Files</th>`;
            html += `</tr>`;
            html += `</thead>`;

            // Init table body
            html += `<tbody id="download-table">`;
            // Populate the table body
            html += getTableBody(json);

            html += `</tbody>`;
            html += `</table>`;

            // Init download button
            html += `<div style="text-align: center; margin: 25px">`;
            let zipURL = `${self.url}/getIntegrityReportsAsZIP?collectionID=${self.collectionID}`;
            html += `<a href="${zipURL}" class="download-button">Download Reports</a>`;
            html += `</div>`;

            // Assign content and apply listener functions.
            $(contentElement).html(html);
            updateDownloadLink(zipURL);
            enableOnClickFunctionality(self);
        }).fail(function () {
            let html = "<div class=\"alert alert-error\">";
            html += "Failed to load page";
            html += "</div>";
            $(contentElement).html(html);
        });
    }

    function getTableBody(json) {
        let html = ``;
        let pillars = Object.keys(json);

        for (let i = 0; i < pillars.length; i++) {
            html += `<tr style="border-top: 1px solid #9996">`;
            // Pillar information
            html += `<td style="border-right: 1px solid #9996;">${pillars[i]}</td>`;

            // Integrity Information
            html += getReportPartTD(json, pillars[i], "missingFile");
            html += getReportPartTD(json, pillars[i], "missingChecksum");
            html += getReportPartTD(json, pillars[i], "obsoleteChecksum");
            html += getReportPartTD(json, pillars[i], "checksumIssue");
            html += getReportPartTD(json, pillars[i], "deletedFile");

            html += `</tr>`;
        }

        return html;
    }

    function getReportPartTD(json, pillarID, reportPart) {
        let html = "";
        if (json[pillarID].includes(reportPart)) {
            html += `<td style="border-right: 1px solid #ecf275; text-align:center; background-color: #bde9ba;">
                <input type="checkbox" name="report-checkbox" value="${reportPart}-${pillarID}" style="margin: 0"/></td>`;
        } else {
            html += `<td style="border-right: 1px solid #9996; text-align:center; background-color: #e5e5e5;"></td>`;
        }
        return html;
    }

    function updateDownloadLink(zipURL) {
        $("a[class=download-button]").on("click", function (e) {
            e.originalEvent.currentTarget.href = zipURL + getSelected();
        });
    }

    function getSelected() {
        let output = "";
        $("input:checkbox[name=report-checkbox]:checked").each(function () {
            output += `&reports=${$(this).val()}`;
        });

        return output;
    }

    function enableOnClickFunctionality(self) {
        // On click refresh table
        $("span#refresh").on("click", function () {
            self.getModal();
        });

        // On click either select all or deselect all
        $("input:checkbox[name=select-all]").change(function () {
            if (this.checked) {
                changeAllCheckboxes(true);
            } else {
                changeAllCheckboxes(false);
            }
        });
    }

    function changeAllCheckboxes(bool) {
        $("input:checkbox").each(function () {
            $(this).prop("checked", bool);
        });
    }
}
