
<%@page import="org.bitrepository.webservice.ServiceUrlFactory" %>
<%@page import="org.bitrepository.webservice.ServiceUrl" %>
<html>
<link type="text/css" href="css/ui-lightness/jquery-ui-1.8.16.custom.css" rel="Stylesheet" />	
<script type="text/javascript" src="js/jquery-1.6.2.min.js"></script>
<script type="text/javascript" src="js/jquery-ui-1.8.16.custom.min.js"></script>
<script type="text/javascript" src="defaultText.js"></script>

	<script>
	    $(function() {
		    $("#putFileForm").buttonset();
	    });
    </script>

    <div class=ui-widget id=putFileTab>
        <form id="putFileForm" action="javascript:submit()">
            <p><b>Put file</b></p>
            <table border="0">
                <tr>
                    <td>Filename:</td>
                    <td>&nbsp;</td>
                    <td><input class="defaultText" title="Filename" id="putFilename" type="text"/></td>
                </tr>
                <tr>
                    <td>Fileaddress:</td>
                    <td>&nbsp;</td>
                    <% ServiceUrl su = ServiceUrlFactory.getInstance(); %>
                    <td> <input type="text" id="putFileaddr" value="<%= su.getDefaultHttpServerUrl() %>"/></td>
                </tr>
                <tr>
                    <td>Filesize:</td>
                    <td>&nbsp;</td>
                    <td><input class="defaultText" title="Filesize" id="putFilesize" type="text"/>
                </tr>
                                <tr>
                    <td>Checksum:</td>
                    <td>&nbsp;</td>
                    <td><input id="putChecksum" type="text"/></td>
                </tr>
                <tr valign="top">
                    <td>Checksum type:</td>
                    <td>&nbsp;</td>
                    <td> 
                        <input type="radio" name="putChecksumType" id="putmd5ChecksumType" value="md5" checked="yes"/> 
                        <label for="putmd5ChecksumType">md5</label>
                        <input type="radio" name="putChecksumType" id="putsha1ChecksumType" value="sha1"/> 
                        <label for="putsha1ChecksumType">sha1</label>
                    </td>
                </tr>
                <tr>
                    <td>Salt (optional):</td>
                    <td>&nbsp;</td>
                    <td><input type="text" id="putChecksumSalt"/></td>
                </tr>
                <tr valign="top">
                    <td>Checksum type:</td>
                    <td>&nbsp;</td>
                    <td> 
                        <input type="radio" name="approveChecksumType" id="approveDisableChecksum" value="disabled" checked="yes"/> 
                        <label for="approveDisableChecksum">disable</label>
                        <input type="radio" name="approveChecksumType" id="approvemd5ChecksumType" value="md5"/> 
                        <label for="approvemd5ChecksumType">md5</label>
                        <input type="radio" name="approveChecksumType" id="approvesha1ChecksumType" value="sha1"/> 
                        <label for="approvesha1ChecksumType">sha1</label>
                    </td>
                </tr>
                <tr>
                    <td>Salt (optional):</td>
                    <td>&nbsp;</td>
                    <td><input type="text" id="approveChecksumSalt"/></td>
                </tr>
            </table> 
            <input type="submit" value="Put file"/>
        </form> 
    </div>
    <div id="messagediv"></div>
        
    <script>
        $("#putFileForm").submit(function() {
            var fileName = $("#putFilename").val();
            var fileAddr = $("#putFileaddr").val();
            var fileSize = $("#putFilesize").val();
            var verifyChecksumVal = $("#putChecksum").val();
            var verifyChecksumType = $("input[name=putChecksumType]:checked").val();
            var verifyChecksumSalt = $("#putChecksumSalt").val();
            var approveChecksumType = $("input[name=approveChecksumType]:checked").val();
            var approveChecksumSalt = $("#approveChecksumSalt").val();      
    
            if (fileName == "") {
                //$('#messagediv').html("<p2>Invalid filename!</p2>").show().fadeOut(5000);
                return false;
            }
            if (fileAddr == "") {
                //$('#messagediv').html("<p2>Invalid address!</p2>").show().fadeOut(5000);
                return false;
            }
            if(fileSize == "") {
                //$('#messagediv').html("<p2>Invalid filesize!</p2>").show().fadeOut(5000);
                return false;
            }
            if(approveChecksumSalt == "disabled") {
            	approveChecksumSalt = "";
            }
            var command = "repo/reposervice/putfile/?fileID=" + fileName + "&url=" + fileAddr + "&fileSize=" + fileSize +
            		"&putChecksum=" + verifyChecksumVal + "&putChecksumType=" + verifyChecksumType + "&putSalt=" + 
            		verifyChecksumSalt + "&approveChecksumType=" + requestChecksumType + "&approveSalt=" + 
            		requestChecksumSalt;
            $('#messagediv').load(command).show().fadeOut(5000);
            return true;
        });
    </script>   
    
    <script>
        function submit() { return ; }
    </script>    
</html>
