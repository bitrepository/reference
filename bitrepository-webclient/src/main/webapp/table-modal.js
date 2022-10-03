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

function TableModal(operation, pillarID, url, contentElement, getTotalItemCountCallback, pageSize) {
    this.url = url;
    this.getTotalItemCountCallback = getTotalItemCountCallback;
    this.pageSize = pageSize;
    let pillars;
    let files;

    this.getModal = function (page) {
        let self = this;
        let maxPages = Math.ceil(this.getTotalItemCountCallback() / this.pageSize);

        $.getJSON(this.url + "&pageSize=" + this.pageSize + "&page=" + page, {}, function (json) {
            //let html = `<div style="padding : 15px">`;
            let html = ``;

            // Initialize pillar and files information
            pillars = [];
            for (let key in json) {
                if (key === pillarID) {
                    // Ensures the pillar in focus is the first pillar in the list
                    pillars.unshift(key);
                } else {
                    pillars.push(key);
                }
            }
            files = json[pillarID];

            // Create search bar
            html += `<div class="fixed-div"><input type="text" class="search-bar" placeholder="Search for file..">`;

            // Create 'Next' and 'Prev' button
            html += `<div class="inline-block">`;
            if (page === 1) {
                html += `<button class="prev-button" disabled>Previous</button>`;
            } else {
                html += `<button class="prev-button">Previous</button>`;
            }
            if (page === maxPages) {
                html += `<button class="next-button" disabled>Next</button>`;
            } else {
                html += `<button class="next-button">Next</button>`;
            }
            // Show [current / total] pages.
            html += `<p class="current-page-p">Page ${page} / ${maxPages}</p>`;
            html += `</div>`;
            html += `</div>`;

            // Create table
            html += `<div class="table-div">`;
            html += `<table class="modal-table" style="width: 100%; border-collapse: separate;">`;

            // Populate the header of the table.
            let header = `<thead class="modal-table-head">`;
            header += `<tr style="padding: 5px; border-bottom: 1pt solid black;">`;
            header += `<th style="text-align: left; border-left: 1px solid white;">#</th>`;
            header += `<th style="text-align: left; padding-left: 5px; width: 35%">File ID</th>`;

            // Color header yellow for the pillar that is in 'focus'
            for (let i = 0; i < pillars.length; i++) {
                if (pillars[i] === pillarID) {
                    header += `<th style="background-color: #ecf275;">${pillars[i].toUpperCase()}</th>`;
                } else {
                    header += `<th>${pillars[i].toUpperCase()}</th>`;
                }
            }

            header += `</tr>`;
            header += `</thead>`;
            html += header + `<tbody id="files-table">`

            // Populate the file status rows of the table. This only happens if there are more than 0 files.
            let idxIncrement = (page - 1) * pageSize;
            html += getTableBody(operation, json, idxIncrement);

            html += `</tbody>`;
            html += `</table>`;
            html += `<p class="no-result-p" style="text-align:center; margin-top: 15px; display: none;">No results found</p>`;
            html += `</div>`;
            //html += `</div>`;

            // Assign html and activate searchbar filtering and copy to clipboard.
            $(contentElement).html(html);
            activateSearchbar();
            enableCopyToClipboard();

            // Enable button functionality
            if (page > 1) {
                $(".prev-button").on("click", () => self.getModal(page - 1));
            }
            if (page < maxPages) {
                $(".next-button").on("click", () => self.getModal(page + 1));
            }
        }).fail(function () {
            let html = "<div class=\"alert alert-error\">"
            html += "Failed to load page";
            html += "</div>"
            $(contentElement).html(html);
        });
    };

    function getTableBody(operation, json, startIdx) {
        let html = ``;

        for (let i = 0; i < files.length; i++) {
            html += `<tr style="border-top: 1px solid #9996">`;
            html += `<td style="border-right: 1px solid #9996;">${startIdx + i + 1}</td>`;
            html += `<td style="padding-left: 5px;"><a href="javascript:void(0);" class="file-id">${files[i]}</a></td>`;
            for (let j = 0; j < pillars.length; j++) {
                if (!json[pillars[j]].includes(files[i]) || operation === "Total files") {
                    // File is NOT missing
                    html += `<td style="text-align: center; background-color: #bde9ba;">&#x2713;</td>`;
                } else {
                    // File IS missing
                    html += `<td style="text-align: center; background-color: #db7070b0;">x</td>`;
                }
            }
            html += `</tr>`;
        }
        return html;
    }

    function activateSearchbar() {
        $(".search-bar").on("keyup", function () {
            let filter = $(this).val().toUpperCase();
            $("#files-table tr").filter(function () {
                $(this).toggle($(this).text().toUpperCase().indexOf(filter) > -1);
            });
            noSearchResult();
        });
    }

    function noSearchResult() {
        let noResultText = $(".no-result-p");
        let hasResult = false;
        $("#files-table tr").each(function () {
            if (!$(this).is(":hidden")) {
                hasResult = true;
                return false;
            }
        });
        hasResult ? noResultText.hide() : noResultText.show();
    }

    function enableCopyToClipboard() {
        $(".file-id").each(function () {
            $(this).on("click", function () {
                let text = $(this).text();
                navigator.clipboard.writeText(text).then();
            });
        });
    }
}
