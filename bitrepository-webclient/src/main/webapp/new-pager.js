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


function NewPager(url, pagerElement, contentElement) {
    this.url = url;
    this.pagerElement = pagerElement;
    this.contentElement = contentElement;

    this.getPage = function () {
        let self = this;
        $.getJSON(self.url, {}, function (j) {
            let html = `<div style="padding : 15px">`;
            html += `<table style="width: 100%"><tbody>`

            // Initialize pillar and files information
            let pillars = [];
            let files = [];
            for (let key in j) {
                pillars.push(key);
                for (let i = 0; i < j[key].length; i++) {
                    if (!files.includes(j[key][i])) {
                        files.push(j[key][i]);
                        console.log(j[key][i]);
                    }
                }
            }
            pillars.sort();
            files.sort();

            // Populate the header of the table.
            let th = `<tr style="padding: 5px; border-bottom: 1pt solid black;">`
            th += `<th style="text-align: left;">File ID</th>`;

            for (let i = 0; i < pillars.length; i++) {
                th += `<th>${pillars[i].toUpperCase()}</th>`;
            }
            th += '</tr>';
            html += th;

            // Populate the remaining rows of the table.
            // TODO: Remove upperbound and introduce autoload on scroll.
            for (let i = 0; i < files.length; i++) {
                html += `<tr>`
                html += `<td>${files[i]}</td>`
                for (let k = 0; k < pillars.length; k++) {
                    if (j[pillars[k]].includes(files[i])) {
                        html += `<td style="text-align: center; background-color: #bde9ba;">&#x2713;</td>`
                    } else {
                        html += `<td style="text-align: center; background-color: #db7070;">x</td>`
                    }
                }
                html += `</tr>`
            }

            html += "</tbody></table></div>";
            $(contentElement).html(html);
        }).fail(function () {
            let html = "<div class=\"alert alert-error\">"
            html += "Failed to load page";
            html += "</div>"
            $(contentElement).html(html);
        });
    };
}

