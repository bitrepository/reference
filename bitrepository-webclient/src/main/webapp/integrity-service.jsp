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
      <div class="span10">
        <div class="span10" style="height:0px; min-height:0px"></div>
        <div class="span10">
          <h2>Integrity service</h2>
          <legend>
            <span>
              <span id="integrityLegend">
                Integrity information
              </span>
              <span class="pull-right"> 
                Change collection: <select class="input" id="collectionChooser"></select>
              </span>
            </span>
          </legend>
        </div>
        <div class="span10" id="collectionInfoDiv"></div>
        <div class="span10"> 
          <legend>Workflows status</legend>
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
          <form class="form-inline">
            <select id="workflowSelector"></select>
            <button type="submit" class=btn" id="workflowStarter">Start</button>
            <div id="formStatus"></div>          
          </form>
        </div>
        <div class="span10"> 
          <legend>Integrity status</legend>
          <table class="table table-bordered table-striped">
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

  <div id="modalPagerDialog" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="modalPagerLabel" aria-hidden="true">
    <div class="modal-header">
      <button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>
      <h3 id="modalPagerLabel">Modal header</h3>
    </div>
    <div class="modal-body" id="modalPagerBody">
      <p>Loading</p>
    </div>
    <div class="modal-footer" style="padding: 0px">
        <div id="modalPager" style="text-align: center; padding: 0px"></div>
    </div>
  </div>  

  <script type="text/javascript" src="jquery/jquery-1.9.0.min.js"></script>
  <script type="text/javascript" src="bootstrap/js/bootstrap.min.js"></script>
  <script type="text/javascript" src="menu.js"></script>
  <script type="text/javascript" src="pager.js"></script>
  <script type="text/javascript" src="utils.js"></script>

  <script>
    
    var pillars = new Object();
    var workflows = new Object();
    var pager;
    var update_page;

    function loadWorkflows() {
      $.getJSON("<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getWorkflowList/?collectionID=" 
            + getCollectionID(), {}, function(j) {
        for (var i = 0; i < j.length; i++) {
          $("#workflowSelector").append('<option value="' + j[i].workflowID + '">' + j[i].workflowID + '</option>');          
        }
      });
    }

    function makeWorkflowRow(workflowID, nextRun, lastRun, executionInterval, currentState) {
      var html = "";
      html += "<tr><td><a class=\"btn btn-link\" id=\"" + workflowID + "-details\">" + workflowID + "</a></td>";
      html += "<td><div id=\"" + workflowID + "-nextRun\" style=\"padding:5px\">" + nextRun + "</div></td>";
      html += "<td><a class=\"btn btn-link\" id=\"" + workflowID + "-lastRun\">" + lastRun + "</a></td>";   
      html += "<td><div id=\"" + workflowID + "-executionInterval\" style=\"padding:5px\">" + executionInterval + "</div></td>";
      html += "<td><div id=\"" + workflowID + "-currentState\" style=\"padding:5px\">" + currentState + "</div></td></tr>";
      return html;
    }
    
    function updateWorkflowRow(workflowID, nextRun, lastRun, executionInterval, currentState) {
      $("#" + workflowID + "-nextRun").html(nextRun);
      $("#" + workflowID + "-lastRun").html(lastRun);
      $("#" + workflowID + "-executionInterval").html(executionInterval);
      $("#" + workflowID + "-currentState").html(currentState);
    }
    
    function getStoredWorkflowInfo(id, type) {
      var myID = id;
      var myType = type;
      return function() {
        return nl2br(workflows[myID][myType]);
      }
    }
    
    function attachWorkflowInfoButton(id, type) {
      var element;
      var title;
      if(type == "workflowDescription") {
        element = "#" + id + "-details";
        title = "Workflow description";
      } else if(type == "lastRunDetails") {
        element = "#" + id + "-lastRun";
        title = "Last run details";
      }
      if(element != null) {
        $(element).popover({placement : "right",
                            title : title,
                            html : true,
                            content : getStoredWorkflowInfo(id, type)});
      }
    }

    function getWorkflowStatuses() {
      $.getJSON("<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getWorkflowSetup/?collectionID=" 
        + getCollectionID(), {}, function(j) {
        var htmlTableBody;
        for (var i = 0; i < j.length; i++) {
          if(workflows[j[i].workflowID] == null) {
            $("#workflow-status-table-body").append(
                makeWorkflowRow(j[i].workflowID, j[i].nextRun, j[i].lastRun, j[i].executionInterval, j[i].currentState));
            workflows[j[i].workflowID] = {workflowDescription : j[i].workflowDescription,
                                          lastRunDetails : j[i].lastRunDetails};
            attachWorkflowInfoButton(j[i].workflowID, "workflowDescription");
            attachWorkflowInfoButton(j[i].workflowID, "lastRunDetails");
          } else {
            updateWorkflowRow(j[i].workflowID, j[i].nextRun, j[i].lastRun, j[i].executionInterval, j[i].currentState);
            workflows[j[i].workflowID].workflowDescription = j[i].workflowDescription;
            workflows[j[i].workflowID].lastRunDetails = j[i].lastRunDetails;
          }
        }
      });
    }

    function getPagingLimit(id, member) {
      var myID = id;
      var myMember = member;
      return function() {
        return pillars[myID][myMember];
      }
    }
    
    function showModalPager(id, member, title, url) {
      var myTitle = title;
      var myUrl = url;
      var myID = id;
      var myMember = member;
      return function() {
        pager = new Pager(getPagingLimit(myID, myMember), 20, myUrl, "#modalPager", "#modalPagerBody");
        $("#modalPagerLabel").html(myTitle);
        pager.getPage(1)();
        $("#modalPagerDialog").modal('show');
      }
    }

    function attachButtonAction(id, type) {
      var element;
      var title;
      var member;
      var url = "<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/";
      if(type == "Total files") {
        element = "#" + id + "-totalFileCount";
        title = type + " on " + id;
        member = "totalFileCount";
        url += "getAllFileIDs/";
      } else if(type == "Missing files") {
        element = "#" + id + "-missingFiles";
        title = type + " on " + id;
        member = "missingFilesCount";
        url += "getMissingFileIDs/";
      } else if(type == "Checksum errors") {
        element = "#" + id + "-checksumErrors";
        title = type + " on " + id;
        member = "checksumErrorCount";
        url += "getChecksumErrorFileIDs/";
      }
      url += "?pillarID=" + id + "&collectionID=" + getCollectionID();
      if(element != null) {
        $(element).click(showModalPager(id, member, title, url));
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
      var url = "<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getIntegrityStatus/?collectionID=" 
            + getCollectionID();
      $.getJSON(url, {}, function(j){
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
                {workflowID: ID,
                 collectionID: getCollectionID() }).show().fadeOut({duration: 5000});

    }
 
    function getCollectionInformation() {
      var url = "<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getCollectionInformation/?collectionID=" + getCollectionID();
      $.getJSON(url, {}, function(j) {
        var infoHtml = "<span><b> Latest ingest: " + j.lastIngest + " &nbsp; &nbsp; ";
        infoHtml += " Size: " + j.collectionSize + " &nbsp; &nbsp; ";
        infoHtml += " Number of files: " + j.numberOfFiles + "</b></span>";
      
        $("#collectionInfoDiv").html(infoHtml);
      });
      
    }

    function getCollectionID() {
      return $("#collectionChooser").val();
    }

    function clearContent() {
      $("#integrity-status-table-body").empty();
      $("#workflow-status-table-body").empty();
      $("#workflowSelector").empty();      
      pillars = new Object();
      workflows = new Object();
    }

    function initializePage() {
      $.getJSON('repo/reposervice/getCollectionIDs/', {}, function(j) {
        for(var i = 0; i < j.length; i++) {
           $("#collectionChooser").append('<option value="' + j[i] + '">' + j[i] + '</option>');
        }
        
        collectionChanged();        
      });
    }

    function collectionChanged() {
        clearContent();
        $("#integrityLegend").html("Integrity information for collection " + getCollectionID());  
        clearInterval(update_page);
        loadWorkflows();
        getCollectionInformation();
        getWorkflowStatuses();
        getIntegrityStatus();
        update_page = setInterval(function() {
          getWorkflowStatuses(); 
          getIntegrityStatus();
        }, 2500);
    }

    $(document).ready(function(){
      // Load page content
      makeMenu("integrity-service.jsp", "#pageMenu");
      initializePage();
    
      // Setup event / click handling
      $("#workflowStarter").click(function(event) { event.preventDefault(); startWorkflow(); });
      $("#collectionChooser").change(function(event) {event.preventDefault(); collectionChanged();});      
    }); 

    </script>
  </body>
</html>
