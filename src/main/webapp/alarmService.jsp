
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
