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


let pages = [{page : "dashboard.html", title : "Dashboard"},
    {page : "configuration.html", title : "Configuration"},
    {page : "alarm-service.html", title : "Alarm"},
    {page : "integrity-service.html", title : "Integrity"},
    {page : "audit-trail-service.html", title : "Audit trail"},
    {page : "status-service.html", title : "Status"}];

function makeMenu(page, element) {
    let menuHtml = "";
    menuHtml += "<nav class=\"navbar navbar-expand-lg navbar-dark bg-dark\">";
    menuHtml += "<div class=\"container-fluid\">";
    menuHtml += "<a class=\"navbar-brand\" href=\"bitrepository-frontpage.html\">";
    menuHtml += "<img src='static/img/bitreplogo.png' alt='BitRepository Logo' height='28' class='d-inline-block align-text-top'/>";
    menuHtml += " Bitrepository";
    menuHtml += "</a>";
    menuHtml += "<button class=\"navbar-toggler\" type=\"button\" data-bs-toggle=\"collapse\" data-bs-target=\"#navbarNav\" aria-label=\"Toggle navigation\">";
    menuHtml += "<span class=\"navbar-toggler-icon\"></span>";
    menuHtml += "</button>";
    menuHtml += "<div class=\"collapse navbar-collapse\" id=\"navbarNav\">";
    menuHtml += "<ul class=\"navbar-nav\">";
    for (let i = 0; i < pages.length; i++) {
        let linkClass = "nav-link";
        if (pages[i].page === page) {
            linkClass += " active";
        }
        menuHtml += "<li class=\"nav-item\"><a class=\"" + linkClass + "\" href=\"" + pages[i].page + "\">"+ pages[i].title + "</a></li>";
    }
    menuHtml += "</ul>";
    menuHtml += "</div><!--/.navbar-collapse -->";
    menuHtml += "</div>";
    menuHtml += "</nav>";
    $(element).html(menuHtml);
}
