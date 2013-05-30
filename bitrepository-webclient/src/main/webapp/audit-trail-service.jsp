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
    <title>Bitrepository audit-trail service</title>
    <!-- Bootstrap -->
    <link href="bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">
    <link href="datepicker/css/datepicker.css" rel="stylesheet">
  </head>
  <body>
  
  <div id="pageMenu"></div>
  <div class="container-fluid">
    <div class="row-fluid">
      <div class="span11">
          <div class="span11" style="height:0px; min-height:0px"></div>
          <div class="span11"><h2>Audit-trail service</h2></div>
          <div class="span11">
            <form class="form-inline">
              <button type="submit" class="btn" id="collectAuditTrails">Collect audit trails</button>
              <div id="initiatorStatus"></div>
            </form>
          </div>
          <div class="span11">
            <form class="form-inline">
            <legend>Audit trails display filters</legend>
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
            <label> Reporting component: <br>
              <div class="input-append">
                <input class="input-medium" id="componentFilter" type="text" placeholder="ComponentID">
                <button class="btn" id="componentIDClearButton" type="button"><i class="icon-remove"></i></button>
              </div>
            </label>
            <label> Actor: <br>
              <div class="input-append" style="padding: 0px"> 
                <input class="input-medium" id="actorFilter" type="text" placeholder="Actor">
                <button class="btn" id="actorClearButton" type="button"><i class="icon-remove"></i></button>
              </div>
            </label>
            <label> Action: <br>
              <div class="input-append">
                <select id="actionFilter">
                  <option>ALL</option>
                  <option>GET_FILE</option>
                  <option>PUT_FILE</option>
                  <option>DELETE_FILE</option>
                  <option>REPLACE_FILE</option>
                  <option>GET_CHECKSUMS</option>
                  <option>GET_FILEID</option>
                  <option>CHECKSUM_CALCULATED</option> 
                  <option>FILE_MOVED</option> 
                  <option>INTEGRITY_CHECK</option> 
                  <option>INCONSISTENCY</option> 
                  <option>FAILURE</option> 
                  <option>OTHER</option>
                </select>
              </div>
            </label>
            <label> CollectionID: <br>
              <div class="input-append">
                <select class="input-medium" id="collectionIDFilter"></select>
              </div>
            </label>
            <label> Max audit trails: <br>
              <div class="input-append">
                <select class="input-small" id="maxAuditTrails">
                  <option>10</option>
                  <option>20</option>
                  <option>50</option>
                  <option>100</option>
                </select>
              </div>
            </label>
            <label> <br>
              <div class="input-append">
                <button class="btn" id="queryAuditTrails">Filter</button>
              </div>
            </label>
          </form>
        </div>         
        <div class="span11">
          <legend>Audit trails</legend>
          <div id="auditTrailsTableDiv">No request sent yet</div>
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

    function startAuditTrailsCollection() {
      $('#initiatorStatus').load(
          '<%= su.getAuditTrailServiceUrl() %>/audittrails/AuditTrailService/collectAuditTrails/',
          {}
        ).fadeIn("slow");
    }    

    function getAuditTrails() {
      var fromDateStr = $("#fromDate").val();
      var toDateStr = $("#toDate").val();
      var fileIDStr = $("#fileIDFilter").val();
      var component = $("#componentFilter").val();
      var actorStr = $("#actorFilter").val();
      var actionStr = $("#actionFilter").val();
      var collectionIDStr = $("#collectionIDFilter").val();
      var maxAudittrailsStr = $("#maxAuditTrails").val();

      $("#auditTrailsTableDiv").html("Loading audit trails...");

      $.post('<%= su.getAuditTrailServiceUrl() %>/audittrails/AuditTrailService/queryAuditTrailEvents/',
        {fromDate: fromDateStr,
         toDate: toDateStr,
         fileID: fileIDStr,
         reportingComponent: component,
         actor: actorStr,
         action: actionStr,
         collectionID: collectionIDStr,
         maxAudittrails: maxAudittrailsStr}, 
         function(j){
           var htmlTable;
           htmlTable = "<table class=\"table table-bordered table-striped\">";
           htmlTable += "<thead> <tr>";
           htmlTable += "<th style=\"min-width: 100px\">FileID</th>";
           htmlTable += "<th style=\"min-width: 160px\">Reporting component</th>";
           htmlTable += "<th style=\"min-width: 70px\">Actor</th>";
           htmlTable += "<th style=\"min-width: 70px\">Action</th>";
           htmlTable += "<th style=\"min-width: 120px\">Timestamp</th>";
           htmlTable += "<th style=\"min-width: 120px\">Info</th>";
           htmlTable += "<th style=\"min-width: 150px\">Message from client</th>";
           htmlTable += "</tr></thead><tbody>";
           for (var i = 0; i < j.length; i++) {
             htmlTable += "<tr><td>" + j[i].fileID + "</td><td>" + j[i].reportingComponent 
                       + "</td><td>" + j[i].actor + "</td><td>" + j[i].action
                       + "</td><td>" + j[i].timeStamp + "</td><td>" + j[i].info
                       + "</td> <td>" + j[i].auditTrailInfo + "</td></tr>";
           }
           htmlTable += "</tbody></table>"; 
           $("#auditTrailsTableDiv").html(htmlTable);
         }, "json").fail(
           function(jqXHR, textStatus, errorThrown) {
             var htmlElement = "<div class=\"alert alert-error\">";
             htmlElement += "<button type=\"button\" class=\"close\" data-dismiss=\"alert\">&times;</button>";
             htmlElement += "<b>Oh snap!</b> ";
             htmlElement += "Fetching of Audit trails failed with: " + errorThrown + "</div>";
             $("#auditTrailsTableDiv").html(htmlElement);
           });
    }

    function getCollectionIDs() {
      $.getJSON('repo/reposervice/getCollectionIDs/', {}, function(j){
        for(var i = 0; i < j.length; i++) {
          $("#collectionIDFilter").append('<option value="' + j[i] + '">' + j[i] + '</option>');
        }
      });
    }
  

    $(document).ready(function(){
      makeMenu("audit-trail-service.jsp", "#pageMenu");
      getCollectionIDs();
      $("#collectAuditTrails").click(function(event) { event.preventDefault(); startAuditTrailsCollection(); });
      $("#fromDate").datepicker();
      $("#toDate").datepicker();
      $("#toDateClearButton").click(function(event) {event.preventDefault(); clearElement("#toDate")});
      $("#fromDateClearButton").click(function(event) {event.preventDefault(); clearElement("#fromDate")});
      $("#fileIDClearButton").click(function(event) {event.preventDefault(); clearElement("#fileIDFilter")});
      $("#componentIDClearButton").click(function(event) {event.preventDefault(); clearElement("#componentFilter")});
      $("#actorClearButton").click(function(event) {event.preventDefault(); clearElement("#actorFilter")});
      $("#queryAuditTrails").click(function(event) {event.preventDefault(); getAuditTrails()});
    }); 

    </script>
  </body>
</html>
