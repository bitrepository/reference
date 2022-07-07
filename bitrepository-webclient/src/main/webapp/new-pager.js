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


function NewPager(pillarID, url, pagerElement, contentElement) {
    this.pillarID = pillarID;
    this.url = url;

    this.getPage = function () {
        $.getJSON(this.url, {}, function (j) {
            let html = `<div style="padding : 15px">`;

            // Initialize pillar and files information
            let pillars = [];
            let files = [];
            for (let key in j) {
                pillars.push(key);
                for (let i = 0; i < j[key].length; i++) {
                    if (!files.includes(j[key][i])) {
                        files.push(j[key][i]);
                    }
                }
            }
            pillars.sort();
            files.sort(sortFn);

            // Create search bar
            html += `<div class="fixed-div"><input type="text" class="search-bar" placeholder="Search for file.."></div>`;

            // Create table
            html += `<table class="modal-table" style="width: 100%; border-collapse: separate;">`;

            // Populate the header of the table.
            let header = `<thead class="modal-table-head">`;
            header += `<tr style="padding: 5px; border-bottom: 1pt solid black;">`;
            header += `<th style="text-align: left; border-left: 1px solid white;">#</th>`;
            header += `<th style="text-align: left; padding-left: 5px; width: 35%">File ID</th>`;

            for (let i = 0; i < pillars.length; i++) {
                if (typeof pillarID !== 'undefined' && pillars[i] === pillarID) {
                    header += `<th style="background-color: #ecf275;">${pillars[i].toUpperCase()}</th>`;
                } else {
                    header += `<th>${pillars[i].toUpperCase()}</th>`;
                }
            }

            header += `</tr>`;
            header += `</thead>`;
            html += header + `<tbody id="files-table">`

            // Populate the file status rows of the table. TODO: Needs autoload on scroll? Or is it okay to have a large table?
            html += getTableBody(pillars, files, j);

            html += `</tbody>`;
            html += `</table>`;
            html += `<p class="no-result-p" style="text-align:center; margin-top: 15px; display: none;">No results found</p>`;
            html += `</div>`;

            // Assign html and activate searchbar filtering.
            $(contentElement).html(html);
            activateSearchbar();
            enableCopyToClipboard();
        }).fail(function () {
            let html = "<div class=\"alert alert-error\">"
            html += "Failed to load page";
            html += "</div>"
            $(contentElement).html(html);
        });
    };

    function getTableBody(pillars, files, j) {
        let html = ``;

        // When a single pillar modal is opened, the pillarID will be used to show only the missing files.
        if (typeof pillarID !== "undefined") {
            for (let idx = (files.length - 1); 0 <= idx; idx--) {
                if (j[pillarID].includes(files[idx])) {
                    files.splice(idx, 1);
                }
            }
        }

        for (let i = 0; i < files.length; i++) {
            html += `<tr style="border-top: 1px solid #9996">`;
            html += `<td style="border-right: 1px solid #9996;">${i + 1}</td>`;
            html += `<td style="padding-left: 5px;"><a href="javascript:void(0);" class="file-id">${files[i]}</a></td>`;
            for (let k = 0; k < pillars.length; k++) {
                if (j[pillars[k]].includes(files[i])) {
                    html += `<td style="text-align: center; background-color: #bde9ba;">&#x2713;</td>`;
                } else {
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

    function enableCopyToClipboard() {
        $(".file-id").each(function () {
            $(this).on("click", function () {
                let text = $(this).text();
                navigator.clipboard.writeText(text).then();
            });
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

    function sortFn(a, b) {
        let regA = a.replace(/[^a-zA-Z]/g, "");
        let regB = b.replace(/[^a-zA-Z]/g, "");
        if (regA === regB) {
            let numA = parseInt(a.replace(/[^0-9]/g, ""), 10);
            let numB = parseInt(b.replace(/[^0-9]/g, ""), 10);
            return numA === numB ? 0 : numA > numB ? 1 : -1;
        } else {
            return regA > regB ? 1 : -1;
        }
    }
}
