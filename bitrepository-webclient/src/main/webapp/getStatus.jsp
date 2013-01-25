<%--
  #%L
  Bitrepository Webclient
  %%
  Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
<html>
<link type="text/css" href="css/ui-lightness/jquery-ui-1.8.16.custom.css" rel="Stylesheet" />   
<script type="text/javascript" src="js/jquery-1.6.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.8.16.custom.min.js"></script>
<script type="text/javascript" src="defaultText.js"></script>

<style>
        div#monitoring-container { width: 500px; margin: 20px 0; }
        div#monitoring-container table { margin: 1em 0; border-collapse: collapse; width: 100%; }
        div#monitoring-container table td, div#monitoring-container table th { border: 1px solid #eee; padding: .6em 10px; text-align: left; }
        .status-ok { background-color:#3eea5b; }
        .status-warn { background-color:#ecea3a; }
        .status-error { background-color:#f83127; }
        .status-unknown { background-color:#399fff; }
</style>

    <% ServiceUrl su = ServiceUrlFactory.getInstance(); %>
    <div id=monitoring-container class="ui-widget">
        <h1>Monitoring service configuration:</h1>
        <div id="monitoringConfiguration"> </div>
        <hr>
        <h1>Component status:</h1>
        <div id="componentStatus"></div>
        
    </div>
    
    <script>
        function addInfoDialog(content, componentID, element) {
            $(element).unbind('click');
            $(element).click(function(){$('<div />').html(content).dialog({title:componentID + " status message"});});
        }
    
    
        jQuery.fn.updateComponentStatus = function() {
            $.getJSON('<%= su.getMonitoringServiceUrl() %>/monitoring/MonitoringService/getComponentStatus/',{}, function(j){
                var htmlTable;
                htmlTable = "<table id=\"users\" class=\"ui-widget ui-widget-content\">";
                htmlTable += "<thead> <tr class=\"ui-widget-header\">";
                htmlTable += "<th width=\"120\">Component ID</th>";
                htmlTable += "<th width=\"100\">Status</th>";
                htmlTable += "<th width=\"100\">Timestamp</th>";
                htmlTable += "</tr></thead><tbody>";
                for (var i = 0; i < j.length; i++) {
                    var rowClass = "status-ok";
                    if(j[i].status == "WARNING") {
                        rowClass = "status-warn";
                    } 
                    if(j[i].status == "ERROR" || j[i].status == "UNRESPONSIVE") {
                        rowClass = "status-error";
                    }
                    if(j[i].status == "UNKNOWN") { 
                        rowClass = "status-unknown";
                    }
                    htmlTable += "<tr id=\"tr-" + j[i].componentID + "\" class=\"" + rowClass + "\"title=\"" + j[i].info + "\"><td>" + j[i].componentID + "</td><td>" + j[i].status + "</td> <td>" + j[i].timeStamp + "</td></tr>";                    
               }
                htmlTable += "</tbody></table>"; 
                $("#componentStatus").html(htmlTable);
                for(var i = 0; i < j.length; i++) {
                     addInfoDialog(j[i].info, j[i].componentID, "#tr-"+j[i].componentID);
                }
                
            })
        }

    </script>

    <script>
        var update_component_status = setInterval(
        function() {
            $().updateComponentStatus();
            }, 2500);
    </script> 
    
    <script>
        $(function(){
            $().updateComponentStatus();
            $.getJSON('<%= su.getMonitoringServiceUrl() %>/monitoring/MonitoringService/getMonitoringConfiguration/',{}, function(j){
                var htmlTable = "<table id=\"users\" class=\"ui-widget ui-widget-content\">";
                htmlTable += "<thead> <tr class=\"ui-widget-header\">";
                htmlTable += "<th width=\"250\">Configuration option</th>";
                htmlTable += "<th width=\"250\">Value</th>";
                htmlTable += "</tr></thead><tbody>";
                var options = '';
                for (var i = 0; i < j.length; i++) {
                    htmlTable += "<tr><td>" + j[i].confOption + "</td><td>" + j[i].confValue + "</td></tr>";
                }
                htmlTable += "</tbody></table>";
                $("#monitoringConfiguration").html(htmlTable);
            })
        })
    </script>
    
    </div>
</html>
