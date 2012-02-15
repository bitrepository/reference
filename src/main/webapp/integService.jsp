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
        $('#integritySetup').load('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getSchedulerSetup/').fadeIn("slow");
        $('#integrityStatus').load('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getIntegrityStatus/').fadeIn("slow");
        $("#fileIDCheckForm").buttonset();
        $("#checksumCheckForm").buttonset();
    });
</script>

    <div id=integrity-container class="ui-widget">
        <h1>Integrity status:</h1>
        <div id=integrityStatus></div>
        <hr>
        <h1>Integrity service configuration:</h1>
        <div id=integritySetup></div>
        <div id=integrityStarter>
            <form id="fileIDCheckForm" action="javascript:submit()">
                Start fileID check on pillar:
                <input class="defaultText" title="pillarID" id="fileIDCheckPillarID" type="text"/>
                <input type="submit" value="Start"/>
            </form>
            <br>        
            <form id="checksumCheckForm" action="javascript:submit()">
                Start fileID check on pillar:
                <input class="defaultText" title="pillarID" id="checksumCheckPillarID" type="text"/>
                <input type="submit" value="Start"/>
            </form>
        </div>
        <div id="actionStatus"> </div>
    </div>
    
    <script>
        var auto_getalarms = setInterval(
        function() {
            $('#integrityStatus').load('<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/getIntegrityStatus/').fadeIn("slow");
            }, 2500);
    </script> 
    
    <script>
        $("#fileIDCheckForm").submit(function() {
            var ID = $("#fileIDCheckPillarID").val();
            $('#actionStatus').load(
                '<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/startFileIDCheckFromPillar/',
                {pillarID: ID}
                ).fadeIn("slow");
            return true;
        });
    </script> 

    <script>
        $("#checksumCheckForm").submit(function() {
            var ID = $("#checksumCheckPillarID").val();
            $('#actionStatus').load(
                '<%= su.getIntegrityServiceUrl() %>/integrity/IntegrityService/startChecksumCheckFromPillar/',
                {pillarID: ID}
                ).fadeIn("slow");
            return true;
        });
    </script> 
    
   	<script>
  		function submit() { return ; }
  	</script>
    
    
</html>
