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
    	
        $('#auditTrails').load('<%= su.getAuditTrailServiceUrl() %>/audittrails/AuditTrailService/getAllAuditTrails/').fadeIn("slow");
        $("#auditTrailsQuery").buttonset();
        $("#fromDate").datepicker();
        $("#toDate").datepicker();
    });
</script>

    <div id=audit-container class="ui-widget">
        <div id=auditTrailsQuery>
            <h1>Filters for audit trails</h1>
            <form id="auditTrailsQueryForm" action="javascript:submit()">
                <table>
                    <tr>
                        <td>From date: <br> <input id="fromDate" type="text" class="dateInput"></td>
                        <td>To date: <br> <input id="toDate" type="text" class="dateInput"></td>
                        <td>FileID: <br> <input id="fileIDFilter" type="text"/></td>
                        <td>Reporting component: <br> <input id="componentFilter" type="text"/></td>
                        <td>Actor: <br> <input id="actorFilter" type="text"/></td>
                        <td>Action: <br>
                            <select id=actionFilter>
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
                        <td valign="bottom"><input type="submit" value="Filter"/></td>
                    </tr>
                </table>
            </form> 
        </div>
        <h1>Audit trail list:</h1>
        <div id=auditTrails></div>
        <div id="actionStatus"> </div>
    </div>

    <script>
       $("#auditTrailsQueryForm").submit(function() {
            var fromDateStr = $("#fromDate").val();
            var toDateStr = $("#toDate").val();
            var fileIDStr = $("#fileIDFilter").val();
            var component = $("#componentFilter").val();
            var actorStr = $("#actorFilter").val();
            var actionStr = $("#actionFilter").val();
            
            $('#auditTrails').load(
                '<%= su.getAuditTrailServiceUrl() %>/audittrails/AuditTrailService/queryAuditTrailEvents/',
                {fromDate: fromDateStr,
                 toDate: toDateStr,
                 fileID: fileIDStr,
                 reportingComponent: component,
                 actor: actorStr,
                 action: actionStr}
                ).fadeIn("slow");
            return true;
        });
    </script> 
    
   	<script>
  		function submit() { return ; }
  	</script>
    
</html>

