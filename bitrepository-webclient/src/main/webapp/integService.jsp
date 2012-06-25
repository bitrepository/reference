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
        $().updateIntegrityStatus();
        $("#workflowLauncher").buttonset();
    });
</script>

    <div id=integrity-container class="ui-widget">
        <h1>Integrity service configuration:</h1>
        <div id="launcher"> 
            <form id="workflowLauncher" action="javascript:submit()">
                <select id="workflowSelector"></select>
                <input type="submit" value="Start">
            </form>
        </div>
        <div id="actionStatus"></div>
        <hr>
        <div id="integritySetup"></div>
        <hr>
        <h1>Integrity status:</h1>
        <div id="integrityStatus"></div>
        
    </div>
    
    <script>
        $(function(){
            $.getJSON('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getWorkflowList/'
                ,{}, function(j){
                var options = '';
                for (var i = 0; i < j.length; i++) {
                    options += '<option value="' + j[i].workflowID + '">' + j[i].workflowID + '</option>';
                }
                $("select#workflowSelector").html(options);
            })
        })
    </script>
    
    <script>
        $(function(){
            $.getJSON('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getWorkflowSetup/',{}, function(j){
                var htmlTable;
                htmlTable = "<table class=\"ui-widget ui-widget-content\">";
                htmlTable += "<thead> <tr class=\"ui-widget-header\">";
                htmlTable += "<th width=\"200\">Workflow name</th>";
                htmlTable += "<th>Next run</th>";
                htmlTable += "<th>Execution interval</th>";
                htmlTable += "</tr></thead><tbody>";
                for (var i = 0; i < j.length; i++) {
                    htmlTable += "<tr><td>" + j[i].workflowID + "</td><td>" + j[i].nextRun 
                        + "</td> <td>" + j[i].executionInterval + "</td></tr>";
               }
                htmlTable += "</tbody></table>"; 
                $("#integritySetup").html(htmlTable);
            })
        })
    </script>
    
    <script>
        jQuery.fn.updateIntegrityStatus = function() {
            $.getJSON('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getIntegrityStatus/',{}, function(j){
                var htmlTable;
                htmlTable = "<table class=\"ui-widget ui-widget-content\">";
                htmlTable += "<thead> <tr class=\"ui-widget-header\">";
                htmlTable += "<th width=\"100\">PillarID</th>";
                htmlTable += "<th width=\"100\">Total number of files</th>";
                htmlTable += "<th width=\"100\">Number of missing files</th>";
                htmlTable += "<th>Number of checksum errors</th>";
                htmlTable += "</tr></thead><tbody>";
                for (var i = 0; i < j.length; i++) {
                    htmlTable += "<tr><td>" + j[i].pillarID + "</td><td>" + j[i].totalFileCount 
                        + "</td> <td>" + j[i].missingFilesCount + "</td> <td>" + j[i].checksumErrorCount + "</td></tr>";
               }
                htmlTable += "</tbody></table>"; 
                $("#integrityStatus").html(htmlTable);
            })
        }
    </script>
    
    <script>
        var auto_getalarms = setInterval(
        function() {
            $().updateIntegrityStatus();
            }, 2500);
    </script> 
    
    <script>
        $("#workflowLauncher").submit(function() {
            var ID = $("#workflowSelector option:selected").val();
            $('#actionStatus').load(
                '<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/startWorkflow/',
                {workflowID: ID}
                ).fadeIn("slow");
            return true;
        });
    </script> 

   	<script>
  		function submit() { return ; }
  	</script>
    
    
</html>
