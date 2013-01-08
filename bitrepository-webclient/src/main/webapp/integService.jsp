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
        $().updateIntegrityStatus();
        $().updateWorkflowSetup();
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
        jQuery.fn.updateWorkflowSetup = function() {
            $.getJSON('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getWorkflowSetup/',{}, function(j){
                var htmlTable;
                htmlTable = "<table class=\"ui-widget ui-widget-content\">";
                htmlTable += "<thead> <tr class=\"ui-widget-header\">";
                htmlTable += "<th width=\"200\">Workflow name</th>";
                htmlTable += "<th>Next run</th>";
                htmlTable += "<th>Execution interval</th>";
                htmlTable += "<th>Current state</th>";
                htmlTable += "</tr></thead><tbody>";
                for (var i = 0; i < j.length; i++) {
                    htmlTable += "<tr><td>" + j[i].workflowID + "</td>" +
                                "<td>" + j[i].nextRun + "</td>" + 
                                "<td>" + j[i].executionInterval + "</td>" +
                                "<td>" + j[i].currentState + "</td></tr>";
                }
                htmlTable += "</tbody></table>"; 
                $("#integritySetup").html(htmlTable);
            })
        }
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
        var auto_getintegrity = setInterval(
        function() {
            $().updateIntegrityStatus();
            $().updateWorkflowSetup();
            }, 2500);
    </script> 
    
    <script>
        $("#workflowLauncher").submit(function() {
            var ID = $("#workflowLauncher").find("#workflowSelector option:selected").val();
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
