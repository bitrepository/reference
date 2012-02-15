
<%@page import="org.bitrepository.webservice.ServiceUrlFactory" %>
<%@page import="org.bitrepository.webservice.ServiceUrl" %>

<html>
<link type="text/css" href="css/ui-lightness/jquery-ui-1.8.16.custom.css" rel="Stylesheet" />	
<script type="text/javascript" src="js/jquery-1.6.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.8.16.custom.min.js"></script>

<meta charset="utf-8">

	<style media="screen" type="text/css">
	    body, td, input, textarea { font-family: 'Trebuchet MS'; }
	    .tableMain { border: solid 1px #a1a1a1; background-color: #f1f1f1; }
	    .defaultText { width: 300px; }
	    .defaultTextActive { color: #a1a1a1; font-style: italic; }
    </style>
    <style>
        body { font-size: 80%; }
        input.text { margin-bottom:12px; width:95%; padding: .4em; }
        fieldset { padding:0; border:0; margin-top:25px; }
        h1 { font-size: 1.2em; margin: .6em 0; }
        div#integrity-container { width: 800px; margin: 20px 0; }
        div#integrity-container table { margin: 1em 0; border-collapse: collapse; width: 100%; }
        div#integrity-container table td, div#integrity-container table th { border: 1px solid #eee; padding: .6em 10px; text-align: left; }
        div#alarm-container { width: 920px; margin: 20px 0; }
        div#alarm-container table { margin: 1em 0; border-collapse: collapse; width: 100%; }
        div#alarm-container table td, div#alarm-container table th { border: 1px solid #eee; padding: .6em 10px; text-align: left; }
    </style>

    <script>
	$(function() {
		$( "#tabs" ).tabs({
			ajaxOptions: {
				error: function( xhr, status, index, anchor ) {
					$( anchor.hash ).html("Wonkers, something went wrong.. Sorry");
				}
			}
		});
	});
	</script>
	<script>
        var auto_refresh = setInterval(
	    function() {
	    	$('#logdiv').load('repo/reposervice/getShortHtmlLog/').fadeIn("slow");
	    	}, 2500);
    </script> 

<% ServiceUrl su = ServiceUrlFactory.getInstance(); %>

<div id="tabs">
	<ul>
		<li><a href="getFile.jsp">Get file</a></li>
		<li><a href="putFile.jsp">Put file</a></li>
		<li><a href="getChecksum.html">Get checksum</a></li>
    	<li><a href="getFileID.html">Get fileID</a></li>
    	<li><a href="deleteFile.html">Delete file</a></li>
    	<li><a href="replaceFile.jsp">Replace file</a></li>
    	<li><a href="configuration.html">Configuration</a></li>
        <li><a href="alarmService.jsp">Alarms</a></li>
        <li><a href="<%= su.getAuditTrailServiceUrl() %>">Audit</a></li>
        <li><a href="integService.jsp">Integrity check</a></li>
	</ul>
</div>

<div id="log"  class="ui-widget">
  <div id="logdiv" class="ui-widget-content"></div>
  <a href="repo/reposervice/getHtmlLog/"
        class="ui-widget-content"> Get full log</a> 
</div>

</html>
