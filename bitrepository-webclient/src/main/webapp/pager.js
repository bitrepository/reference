
      
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

