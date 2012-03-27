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
        div#monitoring-container table td, div#alarm-container table th { border: 1px solid #eee; padding: .6em 10px; text-align: left; }
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
        jQuery.fn.updateComponentStatus = function() {
            $.getJSON('<%= su.getMonitoringServiceUrl() %>/monitoring/MonitoringService/getComponentStatus/',{}, function(j){
                var htmlTable;
                htmlTable = "<table id=\"users\" class=\"ui-widget ui-widget-content\">";
                htmlTable += "<thead> <tr class=\"ui-widget-header\">";
                htmlTable += "<th width=\"100\">Component ID</th>";
                htmlTable += "<th width=\"100\">Status</th>";
                htmlTable += "<th width=\"300\">Info</th>";
                htmlTable += "</tr></thead><tbody>";
                for (var i = 0; i < j.length; i++) {
                    htmlTable += "<tr><td>" + j[i].componentID + "</td><td>" + j[i].status + "</td> <td>" + j[i].info + "</td></tr>";
               }
                htmlTable += "</tbody></table>"; 
                $("#componentStatus").html(htmlTable);
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
