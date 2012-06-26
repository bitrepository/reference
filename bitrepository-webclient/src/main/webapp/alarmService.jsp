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
            $().updateAlarms();
            //$('#alarmsContent').load('<%= su.getAlarmServiceUrl() %>/alarm/AlarmService/getShortAlarmList/').fadeIn("slow");
        });
    </script>

    <div id="alarm-container" class="ui-widget">
        <h1>Alarms:</h1>
        <div id=alarmsContent> </div>
    </div>

    <script>
        jQuery.fn.updateAlarms = function() {
            $.getJSON('<%= su.getAlarmServiceUrl() %>/alarm/AlarmService/getShortAlarmList/',{}, function(j){
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
        //    $('#alarmsContent').load('<%= su.getAlarmServiceUrl() %>/alarm/AlarmService/getShortAlarmList/').fadeIn("slow");
            }, 2500);
    </script> 
</html>
