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

      
  function Pager(maxCountMethod, pageSize, url, pagerElement, contentElement) {
  
    this.maxCountMethod = maxCountMethod;
    this.pageSize = pageSize;  
    this.url = url;
    this.pagerElement = pagerElement;
    this.contentElement = contentElement;
 
    this.makePager = function(currentPage) {
      var lastPage = Math.ceil(this.maxCountMethod() / this.pageSize);
      var html = "<div class=\"pagination\" style=\"padding: 0px\"><ul style=\"padding: 0px\">";
      if(currentPage == 1) {
        html += "<li class=\"disabled\" style=\"padding: 0px\">";
      } else {
        html += "<li style=\"padding: 0px\">";
      }
      html += "<a id=\"prev-button\" href=\"#\">Prev</a></li>";
      if(currentPage >= lastPage) {
        html += "<li class=\"disabled\" style=\"padding: 0px\">";
      } else { 
        html += "<li style=\"padding: 0px\">";
      }
      html += "<a id=\"next-button\" href=\"#\">Next</a></li>";
      html += "</ul></div>";
      $(pagerElement).html(html);

      if(currentPage < lastPage) {
        $("#next-button").click(this.getPage(currentPage + 1));
      }
      if(currentPage > 1) {
        $("#prev-button").click(this.getPage(currentPage - 1));
      }
    }
 
    this.getPage = function(page) {
      var self = this;
      return function() {
        var firstID = 1 + ((page - 1) * pageSize);
        var lastID = (page) * pageSize;
        var maxCount = self.maxCountMethod();
        if(lastID > maxCount) {
          lastID = maxCount;
        }
        $.getJSON(self.url + "&pageSize=" + self.pageSize + "&pageNumber=" + page,
            {}, function(j){
          var html = "<div style=\"text-align : center\">Showing fileIDs " + firstID 
            + " to " + lastID + " of " + maxCount + "</div>";
          html += "<div style=\"padding : 5px\">";
          for(i = 0; i < j.length; i++) {
            html += j[i] + " <br>";
          }

          html += "</div>";
          $(contentElement).html(html);
          self.makePager(page);
        });
      };
    }
  }

