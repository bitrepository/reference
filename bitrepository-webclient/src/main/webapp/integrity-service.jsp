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
        <div class="span10"></div>
        <div class="span10"><h2>Integrity service</h2></div>
        <div class="span10"> 
          <form class="form-inline">
            <select id="workflowSelector"></select>
            <button type="submit" class=btn" id="workflowStarter">Start</button>
            <div id="formStatus"></div>          
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
    
    function getMsg(id) {
      return function() {
        return "foo " + id;
      }
    } 

    function attachButtonAction(id, type) {
      var element;
      var title;
      if(type == "Total files") {
        element = "#" + id + "-totalFileCount";
        title = type + " on " + id;
      } else if(type == "Missing files") {
        element = "#" + id + "-missingFiles";
        title = type + " on " + id;
      } else if(type == "Checksum errors") {
        element = "#" + id + "-checksumErrors";
        title = type + " on " + id;
      }
      if(element != null) {
        $(element).popover({placement : "right", 
          title: title, 
          content: getMsg(id)}); 
      }
      
    }

    function makePillarRow(id, totalFileCount, missingFilesCount, checksumErrorCount) {
      var html = "";
      html += "<tr id=\"" + id + "-row\">";
      html += "<td><div style=\"padding:5px\">" + id + "</div></td>";
      html += "<td><a class=\"btn btn-link\" id=\"" + id + "-totalFileCount\">" + totalFileCount + "</a></td>";
      html += "<td><a class=\"btn btn-link\" id=\"" + id + "-missingFiles\">" + missingFilesCount + "</a></td>";
      html += "<td><a class=\"btn btn-link\" id=\"" + id + "-checksumErrors\">" + checksumErrorCount + "</a></td></tr>";
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
            pillars[j[i].pillarID] = {totalFileCount : j[i].totalFileCount,
                                      missingFilesCount : j[i].missingFilesCount,
                                      checksumErrorCount : j[i].checksumErrorCount};
            attachButtonAction(j[i].pillarID, "Total files");
            attachButtonAction(j[i].pillarID, "Missing files");
            attachButtonAction(j[i].pillarID, "Checksum errors");
          } else {
            updatePillarRow(j[i].pillarID, j[i].totalFileCount, j[i].missingFilesCount, j[i].checksumErrorCount);
            pillars[j[i].pillarID].totalFileCount = j[i].totalFileCount;
            pillars[j[i].pillarID].missingFilesCount = j[i].missingFilesCount;
            pillars[j[i].pillarID].checksumErrorCount = j[i].checksumErrorCount;
          }
        }
      });
    }

    function startWorkflow() {
      var ID = $("#workflowSelector option:selected").val();
      $('#formStatus').load(
                '<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/startWorkflow/',
                {workflowID: ID}).show().fadeOut({duration: 5000});

    }


    $(document).ready(function(){
      // Load page content
      makeMenu("integrity-service.jsp", "#pageMenu");
      loadWorkflows();
      getWorkflowStatuses();
      getIntegrityStatus();
    
      // Setup event / click handling
      $("#workflowStarter").click(function(event) { event.preventDefault(); startWorkflow(); });
      
      // Add page auto update
      var update_page = setInterval(function() {
        getWorkflowStatuses(); 
        getIntegrityStatus();
      }, 2500);
    }); 

    </script>
  </body>
</html>