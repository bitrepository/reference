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

    this.getModal = function (integrityURL = this.url) {
        $.getJSON(`${integrityURL}/getAvailableIntegrityReports?collectionID=${this.collectionID}`, {}, function (json) {
            let html = `<p style="text-align: center; margin: 0 0 40px;">`;
            html += `Select the reports you wish to download. If none are selected, only the latest full integrity report will be downloaded.`;
            html += `</p>`;

            // Create table
            html += `<table class="modal-table" style="width: 100%; border-collapse: separate;">`;

            // Populate the header of the table.
            html += `<thead class="modal-table-head">`;
            html += `<tr style="padding: 5px; border-bottom: 1pt solid black;">`;
            html += `<th style="text-align: left; border-left: 1px solid white; border-right: 1px solid #9996;">Pillar ID</th>`;
            html += `<th style="border-right: 1px solid #9996;">Missing Files</th>`;
            html += `<th style="border-right: 1px solid #9996;">Missing Checksums</th>`;
            html += `<th style="border-right: 1px solid #9996;">Obsolete Checksums</th>`;
            html += `<th>Inconsistent Checksums</th>`;
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
            let zipURL = `${integrityURL}/getIntegrityReportsAsZIP?collectionID=${collectionID}`;
            html += `<a href="${zipURL}" class="download-button">Download Reports</a>`;
            html += `</div>`;

            // Assign content and apply listener functions.
            $(contentElement).html(html);
            updateDownloadLink(zipURL);
        }).fail(function () {
            let html = "<div class=\"alert alert-error\">";
            html += "Failed to load page";
            html += "</div>";
            $(contentElement).html(html);
        });
    };

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
            // html += getReportPartTD(json, pillars[i], "deletedFile");
            // TODO: Include deletedFiles

            html += `</tr>`;
        }

        // TODO: Create download button that uses "/getIntegrityReportPart"
        return html;
    }

    function getReportPartTD(json, pillarID, reportPart) {
        let html = "";
        if (json[pillarID].includes(reportPart)) {
            html += `<td style="border-right: 1px solid #ecf275; text-align:center; background-color: #bde9ba;">
                <input type="checkbox" name="report-checkbox" value="${reportPart}-${pillarID}" style="margin: 0"></td>`;
        } else {
            html += `<td style="border-right: 1px solid #9996; text-align:center; background-color: #e5e5e5;"></td>`;
        }
        return html;
    }

    function updateDownloadLink(zipURL) {
        $("input:checkbox[name=report-checkbox]").on("change", function () {
            $(".download-button").attr("href", function () {
                return zipURL + getSelected();
            })
        });
    }

    function getSelected() {
        let output = "";
        $("input:checkbox[name=report-checkbox]:checked").each(function () {
            output += `&reports=${$(this).val()}`;
        });

        return output;
    }

    function getTableFooter() {
        // TODO: Footer with "Get all integrity reports" checkbox.
        let footer = "";
        footer += "";
        return footer;
    }
}
