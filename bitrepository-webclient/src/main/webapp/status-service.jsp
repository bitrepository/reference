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
  <script type="text/javascript" src="utils.js"></script>

  <script>
    var components = new Object();
    var monitoringServiceUrl;  
    var update_component_status;

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
      html += "<tr " + classAttr + " id=\"" + id +"-row\">";
      html += "<td>" + id + "</td>";
      html += "<td id=\"" + id + "-status\">" + status + " </td>";
      html += "<td id=\"" + id + "-time\">" + time + "</td>";
      html += "<td> <button class=\"btn btn-mini\" id=\"" + id + "-msg-btn\"> Show details <i class=\"icon-chevron-right\"></i></button></td>";
      html += "<tr>";    
      return html;
    }

    function updateComponentRow(id, status, time) {
      $("#" + id + "-row").removeClass("success alert alert-error info");
      var newClass = "success";
      if(status == "WARNING") {
        newClass = "alert";
      } else if(status == "UNRESPONSIVE" || status == "ERROR") {
        newClass = "alert alert-error";
      } else if(status == "UNKNOWN") {
        newClass = "info";
      }
      $("#" + id + "-row").addClass(newClass);
      $("#" + id + "-status").html(status);
      $("#" + id + "-time").html(time);
    }
        
    function populateStatusServiceConfiguration() {
      var url = monitoringServiceUrl + '/monitoring/MonitoringService/getMonitoringConfiguration/';
      $.getJSON(url, {}, function(j){
        var htmlTableBody = "";
        for (var i = 0; i < j.length; i++) {
          htmlTableBody += "<tr><td>" + j[i].confOption + "</td><td>" + j[i].confValue + "</td></tr>";
        }
        $("#configuration-table-body").html(htmlTableBody);
      });
    }
        
    function getMsg(id) {
      return function() {
        return components[id].msg;
      }
    } 

    function attachButtonAction(id, message) {
      var element = "#" + id + "-msg-btn";
      $(element).popover({placement : "right",
          html: true,  
    	  title: id + " status message", 
          content: getMsg(nl2br(id))});
    }
        
    function getStatuses() {
      var url = monitoringServiceUrl + '/monitoring/MonitoringService/getComponentStatus/';
      $.getJSON(url, {}, function(j){
        for(var i = 0; i < j.length; i++) {
          if(components[j[i].componentID] == null) {
            $("#component-status-table-body").append(
              makeComponentRow(j[i].componentID, j[i].status, j[i].timeStamp));
              components[j[i].componentID] = {msg : j[i].info};
              attachButtonAction(j[i].componentID, j[i].info);
            } else {
              updateComponentRow(j[i].componentID, j[i].status, j[i].timeStamp);
              components[j[i].componentID].msg = j[i].info;
            }
        }
      });
    }

    function initPage() {
      $.get('repo/urlservice/monitoringService', {}, function(url) {
        monitoringServiceUrl = url;
        populateStatusServiceConfiguration();
        getStatuses();
        update_component_status = setInterval(function() {getStatuses(); }, 2500);
      })
    }
        
    $(document).ready(function(){
      makeMenu("status-service.jsp", "#pageMenu");
      initPage();
      /*populateStatusServiceConfiguration();
      getStatuses();
      var update_component_status = setInterval(function() {getStatuses(); }, 2500);*/
    }); 

    </script>
  </body>
</html>
