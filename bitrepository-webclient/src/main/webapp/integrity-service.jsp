<%@page import="org.bitrepository.webservice.ServiceUrlFactory" %>
<%@page import="org.bitrepository.webservice.ServiceUrl" %>
<!DOCTYPE html>
<html>
  <% ServiceUrl su = ServiceUrlFactory.getInstance(); %>
  <head>
    <title>Bitrepository integrity service</title>
    <!-- Bootstrap -->
    <link href="bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">
  </head>
  <body>
  
  <div id="pageMenu"></div>
  <div class="container-fluid">
    <div class="row-fluid">
      <div class="span1"></div>
      <div class="span10">
        <div class="span10"><h2>Integrity service</h2></div>
        <div class="span10">Integrity service </div>
        <div class="span10"> 
          <form class="form-inline">
            <select id="workflowSelector"></select>
            <button type="submit" class=btn">Start</button>
          </form>
        </div>
        <div class="span10"> 
          <table class="table table-bordered">
            <thead>
              <tr>
                <th>Workflow name</th>
                <th>Next run</th>
                <th>Last run</th>
                <th>Interval</th>
                <th>Current state</th>
              </tr>
            </thead>
            <tbody id="workflow-status-table-body"></tbody>
          </table>
        </div>
        <div class="span10"> 
          <h4>Integrity status </h4>
          <table class="table table-bordered">
            <thead>
              <tr>
                <th>Pillar ID</th>
                <th>Total number of files</th>
                <th>Number of missing files</th>
                <th>Number of checksum errors</th>
              </tr>
            </thead>
            <tbody id="integrity-status-table-body"></tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
  <script type="text/javascript" src="jquery/jquery-1.9.0.min.js"></script>
  <script type="text/javascript" src="bootstrap/js/bootstrap.min.js"></script>
  <script type="text/javascript" src="menu.js"></script>

  <script>
    
    var pillars = new Object();

    function loadWorkflows() {
      $.getJSON('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getWorkflowList/'
          ,{}, function(j){
        for (var i = 0; i < j.length; i++) {
          $("#workflowSelector").append('<option value="' + j[i].workflowID + '">' + j[i].workflowID + '</option>');          
        }
      });
    }

    function getWorkflowStatuses() {
      $.getJSON('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getWorkflowSetup/',
          {}, function(j){
        var htmlTableBody;
        for (var i = 0; i < j.length; i++) {
          htmlTableBody += "<tr><td>" + j[i].workflowID + "</td>" +
                       "<td>" + j[i].nextRun + "</td>" +
                       "<td>" + j[i].lastRun + "</td>" +
                       "<td>" + j[i].executionInterval + "</td>" +
                       "<td>" + j[i].currentState + "</td></tr>";
        }
        $("#workflow-status-table-body").html(htmlTableBody);
      });
    }

    function makePillarRow(id, totalFileCount, missingFilesCount, checksumErrorCount) {
      var html = "";
      html += "<tr id=\"" + id + "-row\">";
      html += "<td>" + id + "</td>";
      html += "<td id=\"" + id + "-totalFileCount\">" + totalFileCount + "</td>";
      html += "<td id=\"" + id + "-missingFiles\">" + missingFilesCount + "</td>";
      html += "<td id=\"" + id + "-checksumErrors\">" + checksumErrorCount + "</td></tr>";
      return html;
    }

    function updatePillarRow(id, totalFileCount, missingFilesCount, checksumErrorCount) {
      $("#" + id + "-totalFileCount").html(totalFileCount);
      $("#" + id + "-missingFiles").html(missingFilesCount);
      $("#" + id + "-checksumErrors").html(checksumErrorCount);
    }

    function getIntegrityStatus() {
      $.getJSON('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getIntegrityStatus/',
          {}, function(j){
        var htmlTable;
        for (var i = 0; i < j.length; i++) {
          if(pillars[j[i].pillarID] == null) {
            $("#integrity-status-table-body").append(makePillarRow(j[i].pillarID, 
                j[i].totalFileCount, j[i].missingFilesCount, j[i].checksumErrorCount));
            pillars[j[i].pillarID] = {id : j[i].pillarID};
            //Attach some buttons of sorts..
          } else {
            updatePillarRow(j[i].pillarID, j[i].totalFileCount, j[i].missingFilesCount, j[i].checksumErrorCount);
          }
        }
      });
    }


    $(document).ready(function(){
      makeMenu("integrity-service.jsp", "#pageMenu");
      loadWorkflows();
      getWorkflowStatuses();
      getIntegrityStatus();
      var update_page = setInterval(function() {
        getWorkflowStatuses(); 
        getIntegrityStatus();
      }, 2500);
    }); 

    </script>
  </body>
</html>