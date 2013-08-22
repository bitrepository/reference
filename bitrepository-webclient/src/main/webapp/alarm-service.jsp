<%--
  #%L
  Bitrepository Webclient
  %%
  Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
  %%
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as 
  published by the Free Software Foundation, either version 2.1 of the 
  License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Lesser Public License for more details.
  
  You should have received a copy of the GNU General Lesser Public 
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/lgpl-2.1.html>.
  #L%
  --%>
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
      <div class="span11">
        <div class="span11" style="height:0px; min-height:0px"></div>
        <div class="span11"><h2>Alarm service</h2></div>
        <div class="span11"> 
          <form class="form-inline">
            <legend>Alarm display filters</legend>
            <label> From date: <br>
              <div class="input-append">
                <input class="input-small" type="text" id="fromDate" placeholder="From date">
                <button class="btn" id="fromDateClearButton" type="button"><i class="icon-remove"></i></button>
              </div>
            </label>
            <label> To date: <br>
              <div class="input-append">
                <input class="input-small" type="text" id="toDate" placeholder="To date">
                <button class="btn" id="toDateClearButton" type="button"><i class="icon-remove"></i></button>
              </div>
            </label>
            <label> FileID: <br>
              <div class="input-append">
                <input id="fileIDFilter" type="text" placeholder="FileID">
                <button class="btn" id="fileIDClearButton" type="button"><i class="icon-remove"></i></button>
              </div>
            </label>              
            <label> Component: <br>
              <div class="input-append">
                <input id="componentFilter" type="text" placeholder="ComponentID">
                <button class="btn" id="componentIDClearButton" type="button"><i class="icon-remove"></i></button>
              </div>
            </label>
            <label> Alarm code: <br>
              <div class="input-append">
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
              </div>
            </label>
            <label> CollectionID: <br>
              <div class="input-append">
                <select class="input-medium" id="collectionIDFilter">
                  <option>ALL</option>
                </select>
              </div>
            </label>
            <label> Max alarms: <br>
              <div class="input-append">
                <select class="input-small" id="maxAlarms">
                  <option>10</option>
                  <option>20</option>
                  <option>50</option>
                  <option>100</option>
                </select>
              </div>
            </label>
          </form>
        </div>
        <div class="span11">  
          <legend>Alarms</legend>
          <table class="table table-bordered table-striped">
            <thead>
              <tr>
                <th>Date</th>
                <th>Component</th>
                <th>CollectionID</th>
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
  <script type="text/javascript" src="utils.js"></script>

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
      var collectionIDStr = $("#collectionIDFilter").val();
            
      $.post('<%= su.getAlarmServiceUrl() %>/alarm/AlarmService/queryAlarms/',
        {fromDate: fromDateStr,
         toDate: toDateStr,
         fileID: fileIDStr,
         reportingComponent: component,
         alarmCode: alarmCodeStr,
         maxAlarms: maxAlarmStr,
         collectionID: collectionIDStr,
         oldestAlarmFirst: false}, function(j){
        var htmlTableBody = "";
        if(j != null) {
          for (var i = 0; i < j.length; i++) {
            htmlTableBody += "<tr><td>" + j[i].OrigDateTime + 
                             "</td><td>" + j[i].AlarmRaiser +
                             "</td><td>" + j[i].CollectionID +
                             "</td> <td>" + j[i].AlarmCode + 
                             "</td> <td>" + nl2br(j[i].AlarmText) +
                             "</td></tr>";
          }
        }
        $("#alarms-table-body").html(htmlTableBody);
      })
    }

    function getCollectionIDs() {
      $.getJSON('repo/reposervice/getCollectionIDs/', {}, function(j){
        for(var i = 0; i < j.length; i++) {
          $("#collectionIDFilter").append('<option value="' + j[i] + '">' + j[i] + '</option>');
        }
      });
    }

    $(document).ready(function(){
      makeMenu("alarm-service.jsp", "#pageMenu");
      getCollectionIDs();
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
