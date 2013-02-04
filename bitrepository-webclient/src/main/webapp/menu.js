

        var pages = [{page : "alarm-service.jsp", title : "Alarm"},
                     {page : "integrity-service.jsp", title : "Integrity"}, 
                     {page : "audit-trail-service.jsp", title : "Audit trail"}, 
                     {page : "status-service.jsp", title : "Status"}];

        function makeMenu(page, element) {
          var menuHtml = "";
          menuHtml += "<div class=\"navbar navbar-inverse navbar-static-top\">";
          menuHtml += "<div class=\"navbar-inner\">";
          menuHtml += "<div class=\"container\">";
          menuHtml += "<a class=\"btn btn-navbar\" data-toggle=\"collapse\" data-target=\".nav-collapse\">";
          menuHtml += "<span class=\"icon-bar\"></span>";
          menuHtml += "<span class=\"icon-bar\"></span>";
          menuHtml += "<span class=\"icon-bar\"></span>";
          menuHtml += "</a>";
          menuHtml += "<a class=\"brand\" href=\"#\">Bitrepository</a>";
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
