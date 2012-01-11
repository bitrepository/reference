
<%@page import="org.bitrepository.webservice.ServiceUrlFactory" %>
<%@page import="org.bitrepository.webservice.ServiceUrl" %>
<html>
<link type="text/css" href="css/ui-lightness/jquery-ui-1.8.16.custom.css" rel="Stylesheet" />	
<script type="text/javascript" src="js/jquery-1.6.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.8.16.custom.min.js"></script>
<script type="text/javascript" src="defaultText.js"></script>


	<script>
	    $(function() {
		    $("#replaceFileForm").buttonset();
	    });
    </script>

    <div class=ui-widget id=replaceFileTab>
        <form id="replaceFileForm">
            Dummy replace ;) <br>
            <p><b>Replace file</b></p>
            <table border="0">
                <tr>
                    <td>Old file ID:</td>
                    <td><input class="defaultText" title="Old file ID" id="oldFilename" type="text"/></td>
                </tr>
                <tr>
                    <td>Filename:</td>
                    <td><input class="defaultText" title="Filename" id="putFilename" type="text"/></td>
                </tr>
                <tr>
                    <td>Fileaddress:</td>
                    <% ServiceUrl su = ServiceUrlFactory.getInstance(); %>
                    <td> <input type="text" id="putFileaddr" value="<%= su.getDefaultHttpServerUrl() %>"/></td>
                </tr>
                <tr>
                    <td>Filesize:</td>
                    <td><input class="defaultText" title="Filesize" id="putFilesize" type="text"/>
                </tr>
            </table> 
            <input type="submit" value="Replace file"/>
        </form> 
    </div>
    <div id="messagediv"></div>
    
    
    <script>
        $("#replaceFileForm").submit(function() {

            return true;
        });
    </script>       
</html>
