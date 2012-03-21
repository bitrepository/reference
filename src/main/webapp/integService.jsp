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
        $('#launcher').load('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getWorkflowLauncher/').fadeIn("slow");
        $('#integritySetup').load('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getWorkflowSetup/').fadeIn("slow");
        $('#integrityStatus').load('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getIntegrityStatus/').fadeIn("slow");
        $("#workflowLauncher").buttonset();
    });
</script>

    <div id=integrity-container class="ui-widget">
        <h1>Integrity service configuration:</h1>
        <div id="launcher"> </div>
        <div id="integritySetup"></div>
        <div id="actionStatus"></div>
        <hr>
        <h1>Integrity status:</h1>
        <div id="integrityStatus"></div>
        
    </div>
    
    <script>
        var auto_getalarms = setInterval(
        function() {
            $('#integrityStatus').load('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getIntegrityStatus/').fadeIn("slow");
            }, 2500);
    </script> 
    
    <script>
        $('#workflowLauncher').submit(function() {
            var ID = $("#workflowSelector option:selected").val();
            $('#actionStatus').load(
                '<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/startWorkflow/',
                {pillarID: ID}
                ).fadeIn("slow");
            return true;
        });
    </script> 

   	<script>
  		function submit() { return ; }
  	</script>
    
    
</html>
