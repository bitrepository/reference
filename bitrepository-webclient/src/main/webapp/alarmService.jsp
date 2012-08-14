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

<% ServiceUrl su = ServiceUrlFactory.getInstance(); %>
    <script>
        $(function() {
            $("#fromDate").datepicker();
            $("#toDate").datepicker();
            $().updateAlarms();
        });
    </script>

    <div id="alarm-container" class="ui-widget">
            <div id=alarmQuery>
            <h1>Filters for displayed alarms:</h1>
            <table>
                <tr>
                    <td>From date: <br> <input id="fromDate" type="text" class="dateInput"></td>
                    <td>To date: <br> <input id="toDate" type="text" class="dateInput"></td>
                    <td>FileID: <br> <input id="fileIDFilter" type="text"/></td>
                    <td>Reporting component: <br> <input id="componentFilter" type="text"/></td>
                    <td>Alarmcode: <br>
                        <select id=alarmCodeFilter>
                            <option>ALL</option>
                            <option>INCONSISTENT_REQUEST</option>
                            <option>COMPONENT_FAILURE</option>
                            <option>CHECKSUM_ALARM</option>
                            <option>FAILED_TRANSFER</option>
                            <option>FAILED_OPERATION</option>
                            <option>INVALID_MESSAGE</option>
                            <option>INVALID_MESSAGE_VERSION</option>
                            <option>TIMEOUT</option>                              
                        </select>
                    </td>
                    <td>Max alarms: <br>
                        <select id=maxAlarms>
                            <option>10</option>
                            <option>20</option>
                            <option>50</option>
                            <option>100</option>
                        </select>
                    </td> 
                </tr>
            </table>
        </div>
        <h1>Alarms:</h1>
        <div id="alarmsContent"> </div>
    </div>

    <script>
        jQuery.fn.updateAlarms = function() {          
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
                 newestAlarmFirst: true}, function(j){
                    var htmlTable;
                    htmlTable = "<table id=\"users\" class=\"ui-widget ui-widget-content\">";
                    htmlTable += "<thead> <tr class=\"ui-widget-header\">";
                    htmlTable += "<th width=\"70\">Date</th>";
                    htmlTable += "<th width=\"80\">Raiser</th>";
                    htmlTable += "<th width=\"80\">Alarm code</th>";
                    htmlTable += "<th>Description</th>";
                    htmlTable += "</tr></thead><tbody>";
                    for (var i = 0; i < j.Alarm.length; i++) {
                        htmlTable += "<tr><td>" + j.Alarm[i].OrigDateTime + "</td><td>" + j.Alarm[i].AlarmRaiser +
                            "</td> <td>" + j.Alarm[i].AlarmCode + "</td> <td>" + j.Alarm[i].AlarmText + "</td></tr>";
                    }
                    htmlTable += "</tbody></table>"; 
                    $("#alarmsContent").html(htmlTable);
            })
        }
    </script>

    <script>
        var auto_getalarms = setInterval(
        function() {
            $().updateAlarms();
            }, 2500);
    </script> 
</html>
