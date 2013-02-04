<%@page import="org.bitrepository.webservice.ServiceUrlFactory" %>
<%@page import="org.bitrepository.webservice.ServiceUrl" %>
<!DOCTYPE html>
<html>
  <% ServiceUrl su = ServiceUrlFactory.getInstance(); %>
  <head>
    <title>Bitrepository status service</title>
    <!-- Bootstrap -->
    <link href="bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">
  </head>
  <body>
  
  <div id="pageMenu"></div>
  <div class="container">
    <div class="row">
      <div class="span9"><h2>Status service</h2></div>
      <div class="span9">   
        <div class="accordion" id="configuration-accordion">  
          <div class="accordion-group">
            <div class="accordion-heading">
              <a class="accordion-toggle" data-toggle="collapse" data-parent="#configuration-accordion" href="#collapseOne"> 
                Show monitoring service configuration <i class="icon-chevron-down"></i>
              </a>
            </div>
            <div id="collapseOne" class="accordion-body collapse">
              <div class="accordion-inner" id="configuration-table">
                <table class="table table-bordered">
                  <thead>
                    <tr>
                      <th>Configuration option</th>
                      <th>Value</th>
                    </tr>
                  </thead>
                  <tbody id="configuration-table-body"></tbody>
                </table> 
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div class="row">
      <div class="span9"> 
        <h>Component status:</h>
        <table class="table table-bordered">
          <thead>
            <tr>
              <th>Component ID</th>
              <th>Status</th>
              <th>Timestamp</th>
              <th>Message</th>
            </tr>
          </thead>
          <tbody id="component-status-table-body"></tbody>
        </table>
      </div>
    </div>
  </div>
  <script type="text/javascript" src="jquery/jquery-1.9.0.min.js"></script>
  <script type="text/javascript" src="bootstrap/js/bootstrap.min.js"></script>
  <script type="text/javascript" src="menu.js"></script>

  <script>
    function makeComponentRow(id, status, time) {
      var html = "";
      var classAttr = "class=\"success\"";
      if(status == "WARNING") {
        classAttr = "class=\"alert\"";
      } else if(status == "UNRESPONSIVE" || status == "ERROR") {
        classAttr = "class=\"alert alert-error\"";
      } else if(status == "UNKNOWN") {
        classAttr = "class=\"info\"";
      }
      html += "<tr " + classAttr + ">";
      html += "<td>" + id + "</td>";
      html += "<td>" + status + " </td>";
      html += "<td>" + time + "</td>";
      html += "<td> <button class=\"btn btn-mini\" id=\"" + id + "-msg-btn\"> Show details <i class="icon-chevron-right"></i></button></td>";
      html += "<tr>";    
      return html;
    }
        
    function populateStatusServiceConfiguration() {
      $.getJSON('<%= su.getMonitoringServiceUrl() %>/monitoring/MonitoringService/getMonitoringConfiguration/', {}, function(j){
        var htmlTableBody = "";
        for (var i = 0; i < j.length; i++) {
          htmlTableBody += "<tr><td>" + j[i].confOption + "</td><td>" + j[i].confValue + "</td></tr>";
        }
        $("#configuration-table-body").html(htmlTableBody);
      });
    }
        
    function attachButtonAction(id, message) {
      var element = "#" + id + "-msg-btn";
      $(element).popover({placement : "right", 
    	  title: id + " status message", 
          content: message});
    }
        
    function getStatuses() {
      $.getJSON('<%= su.getMonitoringServiceUrl() %>/monitoring/MonitoringService/getComponentStatus/',{}, function(j){
        var htmlTableBody = "";
        for (var i = 0; i < j.length; i++) {
          htmlTableBody += makeComponentRow(j[i].componentID, j[i].status, j[i].timeStamp);                    
        }
        $("#component-status-table-body").html(htmlTableBody);
        for(var i = 0; i < j.length; i++) {
          attachButtonAction(j[i].componentID, j[i].info);
        }
      });
    }
        
    $(document).ready(function(){
      makeMenu("status-service.html", "#pageMenu");
      populateStatusServiceConfiguration();
      getStatuses();
      var update_component_status = setInterval(function() {getStatuses(); }, 2500);
    }); 

    </script>
  </body>
</html>
