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


        var pages = [{page : "dashboard.jsp", title : "Dashboard"},
                     {page : "configuration.html", title : "Configuration"}, 
                     {page : "alarm-service.html", title : "Alarm"},
                     {page : "integrity-service.html", title : "Integrity"}, 
                     {page : "audit-trail-service.html", title : "Audit trail"}, 
                     {page : "status-service.html", title : "Status"}];

        function makeMenu(page, element) {
          var menuHtml = "";
          menuHtml += "<div class=\"navbar navbar-inverse navbar-static-top\">";
          menuHtml += "<div class=\"navbar-inner\">";
          menuHtml += "<div class=\"container-fluid\">";
          menuHtml += "<a class=\"btn btn-navbar\" data-toggle=\"collapse\" data-target=\".nav-collapse\">";
          menuHtml += "<span class=\"icon-bar\"></span>";
          menuHtml += "<span class=\"icon-bar\"></span>";
          menuHtml += "<span class=\"icon-bar\"></span>";
          menuHtml += "</a>";
          menuHtml += "<a class=\"brand\" href=\"bitrepository-frontpage.html\">Bitrepository</a>";
          menuHtml += "<div class=\"nav-collapse collapse\">";
          menuHtml += "<ul class=\"nav\">";
          for(var i=0; i<pages.length; i++) {
            linkClass="";
            if(pages[i].page == page) {
              linkClass="class=\"active\"";
            }
            menuHtml += "<li " + linkClass +"><a href=\"" + pages[i].page + "\">"+ pages[i].title + "</a></li>";            
          }
          menuHtml += "</ul>";
          menuHtml += "</div><!--/.nav-collapse -->";
          menuHtml += "</div>";
          menuHtml += "</div>";
          menuHtml += "</div>";
          $(element).html(menuHtml);
        }
