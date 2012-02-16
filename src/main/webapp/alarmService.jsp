
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
            $('#alarmsContent').load('<%= su.getAlarmServiceUrl() %>/alarm/AlarmService/getShortAlarmList/').fadeIn("slow");
        });
    </script>

    <div id="alarm-container" class="ui-widget">
        <h1>Alarms:</h1>
        <div id=alarmsContent>
    </div>

    <script>
        var auto_getalarms = setInterval(
        function() {
            $('#alarmsContent').load('<%= su.getAlarmServiceUrl() %>/alarm/AlarmService/getShortAlarmList/').fadeIn("slow");
            }, 2500);
    </script> 
</html>
