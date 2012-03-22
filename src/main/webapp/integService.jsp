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
        $('#integritySetup').load('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getWorkflowSetup/').fadeIn("slow");
        $('#integrityStatus').load('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getIntegrityStatus/').fadeIn("slow");
        $("#workflowLauncher").buttonset();
    });
</script>

    <div id=integrity-container class="ui-widget">
        <h1>Integrity service configuration:</h1>
        <div id="launcher"> 
            <form id="workflowLauncher" action="javascript:submit()">
                <select id="workflowSelector">
                    <option value=""> asd </option>
                </select>
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
                    options += '<option value="' + j[i].optionValue + '">' + j[i].optionDisplay + '</option>';
                }
                $("select#workflowSelector").html(options);
            })
        })
    </script>
    
    
    <script>
        var auto_getalarms = setInterval(
        function() {
            $('#integrityStatus').load('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getIntegrityStatus/').fadeIn("slow");
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
