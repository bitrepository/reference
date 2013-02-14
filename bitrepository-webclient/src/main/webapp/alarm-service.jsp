<%@page import="org.bitrepository.webservice.ServiceUrlFactory" %>
<%@page import="org.bitrepository.webservice.ServiceUrl" %>
<!DOCTYPE html>
<html>
  <% ServiceUrl su = ServiceUrlFactory.getInstance(); %>
  <head>
    <title>Bitrepository alarm service</title>
    <link href="bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">
    <link href="datepicker/css/datepicker.css" rel="stylesheet">
  </head>
  <body>
  
    <div id="pageMenu"></div>
  <div class="container-fluid">
    <div class="row-fluid">
      <div class="span1"></div>
      <div class="span10">
        <div class="span10"></div>
        <div class="span10"><h2>Alarm service</h2></div>
        <div class="span10"> 
          <form class="form-inline">
            <legend>Alarm display filters</legend>
            <div class="input-append">
              <label> From date: <br>
                <input class="input-small" type="text" id="fromDate" placeholder="From date">
                <button class="btn" id="fromDateClearButton" type="button">Clear</button>
              </label>
            </div>
            <div class="input-append">
              <label> To date: <br>
                <input class="input-small" type="text" id="toDate" placeholder="To date">
                <button class="btn" id="toDateClearButton" type="button">Clear</button>
              </label>
            </div>
            <div class="input-append">
              <label> FileID: <br>
                <input id="fileIDFilter" type="text" placeholder="FileID">
                <button class="btn" id="fileIDClearButton" type="button">Clear</button>
              </label>              
            </div>
            <div class="input-append">
              <label> Reporting component: <br>
                <input id="componentFilter" type="text" placeholder="ComponentID">
                <button class="btn" id="componentIDClearButton" type="button">Clear</button>
              </label>
            </div>
            <div class="input-append">
              <label> Alarm code: <br>
                <select id="alarmCodeFilter">
                  <option>ALL</option>
                  <option>INCONSISTENT_REQUEST</option>
                  <option>COMPONENT_FAILURE</option>
                  <option>CHECKSUM_ALARM</option>
                  <option>FAILED_TRANSFER</option>
                  <option>FAILED_OPERATION</option>
                  <option>INVALID_MESSAGE</option>
                  <option>INVALID_MESSAGE_VERSION</option>
                  <option>TIMEOUT</option>    
                  <option>INTEGRITY_ISSUE</option>
                </select>
              </label>
            </div>
            <div class="input-append">
              <label> Max alarms: <br>
                <select class="input-small" id=maxAlarms>
                  <option>10</option>
                  <option>20</option>
                  <option>50</option>
                  <option>100</option>
                </select>
              </label>
            </div>
          </form>
        </div>
        <div class="span10">  
          <legend>Alarms</legend>
          <table class="table table-bordered table-striped">
            <thead>
              <tr>
                <th>Date</th>
                <th>Raiser</th>
                <th>Alarm code</th>
                <th>Description</th>
              </tr>
            </thead>
            <tbody id="alarms-table-body"></tbody>
          </table>
        </div>
      </div>
    </div>
  </div>
  <script type="text/javascript" src="jquery/jquery-1.9.0.min.js"></script>
  <script type="text/javascript" src="bootstrap/js/bootstrap.min.js"></script>
  <script type="text/javascript" src="datepicker/js/bootstrap-datepicker.js"></script>
  <script type="text/javascript" src="menu.js"></script>

  <script>
    function clearElement(element) {
      $(element).val("");
    }

    function updateAlarms() {
      var fromDateStr = $("#fromDate").val();
      var toDateStr = $("#toDate").val();
      var fileIDStr = $("#fileIDFilter").val();
      var component = $("#componentFilter").val();
      var alarmCodeStr = $("#alarmCodeFilter").val();
      var maxAlarmStr = $("#maxAlarms").val();
            
      $.post('<%= su.getAlarmServiceUrl() %>/alarm/AlarmService/queryAlarms/',
        {fromDate: fromDateStr,
         toDate: toDateStr,
         fileID: fileIDStr,
         reportingComponent: component,
         alarmCode: alarmCodeStr,
         maxAlarms: maxAlarmStr,
         oldestAlarmFirst: false}, function(j){
        var htmlTableBody = "";
        if(j != null) {
          for (var i = 0; i < j.Alarm.length; i++) {
            htmlTableBody += "<tr><td>" + j.Alarm[i].OrigDateTime + 
                             "</td><td>" + j.Alarm[i].AlarmRaiser +
                             "</td> <td>" + j.Alarm[i].AlarmCode + 
                             "</td> <td>" + j.Alarm[i].AlarmText + 
                             "</td></tr>";
          }
        }
        $("#alarms-table-body").html(htmlTableBody);
      })
    }

    $(document).ready(function(){
      makeMenu("alarm-service.jsp", "#pageMenu");
      updateAlarms();
      $("#fromDate").datepicker();
      $("#toDate").datepicker();
      $("#toDateClearButton").click(function(event) {event.preventDefault(); clearElement("#toDate")});
      $("#fromDateClearButton").click(function(event) {event.preventDefault(); clearElement("#fromDate")});
      $("#fileIDClearButton").click(function(event) {event.preventDefault(); clearElement("#fileIDFilter")});
      $("#componentIDClearButton").click(function(event) {event.preventDefault(); clearElement("#componentFilter")});
      var auto_getalarms = setInterval(function() {updateAlarms();}, 2500);
    }); 

    </script>
  </body>
</html>
