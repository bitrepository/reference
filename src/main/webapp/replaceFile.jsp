
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

    <div class=ui-widget>
        <form id="replaceFileForm" action="javascript:submit()">
            <p><b>Replace file</b></p>
            <table border="0">
                <tr>
                    <td>File ID:</td>
                    <td>&nbsp;</td>
                    <td><input class="defaultText" title="File ID" id="fileID" type="text"/></td>
                    <td>&nbsp;&nbsp;</td>
                    <td>Pillar ID:</td>
                    <td>&nbsp;</td>
                    <td><input class="defaultText" title="Pillar ID" id="pillarID" type="text"/></td>
                </tr>
                <tr>
                    <td>Fileaddress:</td>
                    <td>&nbsp;</td>
                    <% ServiceUrl su = ServiceUrlFactory.getInstance(); %>
                    <td> <input type="text" id="fileaddr" value="<%= su.getDefaultHttpServerUrl() %>"/></td>
                    <td>&nbsp;&nbsp;</td>
                    <td>Filesize:</td>
                    <td>&nbsp;</td>
                    <td><input class="defaultText" title="Filesize" id="filesize" type="text"/></td>
                </tr>
                <tr>
                    <td>Old file checksum:</td>
                    <td>&nbsp;</td>
                    <td><input id="oldFileChecksum" type="text"/></td>
                    <td>&nbsp;&nbsp;</td>
                    <td>New file checksum:</td>
                    <td>&nbsp;</td>
                    <td><input id="newFileChecksum" type="text"/></td>
                </tr>
                <tr>
                    <td>Old file checksum type:</td>
                    <td>&nbsp;</td>
                    <td>                        
                        <input type="radio" name="oldFileChecksumType" id="oldFilemd5ChecksumType" value="md5" checked="yes"/> 
                        <label for="oldFilemd5ChecksumType">md5</label>
                        <input type="radio" name="oldFileChecksumType" id="oldFilesha1ChecksumType" value="sha1"/> 
                        <label for="oldFilesha1ChecksumType">sha1</label>
                    </td>
                    <td>&nbsp;&nbsp;</td>
                    <td>New file checksum type:</td>
                    <td>&nbsp;</td>
                    <td>                        
                        <input type="radio" name="newFileChecksumType" id="newFilemd5ChecksumType" value="md5" checked="yes"/> 
                        <label for="newFilemd5ChecksumType">md5</label>
                        <input type="radio" name="newFileChecksumType" id="newFilesha1ChecksumType" value="sha1"/> 
                        <label for="newFilesha1ChecksumType">sha1</label>
                    </td>
                </tr>
                <tr>
                    <td>Old file checksum salt (optional):</td>
                    <td>&nbsp;</td>
                    <td><input type="text" id="oldFileChecksumSalt"/></td>
                    <td>&nbsp;&nbsp;</td>
                    <td>New file checksum salt (optional):</td>
                    <td>&nbsp;</td>
                    <td><input type="text" id="newFileChecksumSalt"/></td>
                </tr>
                <tr>
                    <td>Old file checksum request type: (optional):</td>
                    <td>&nbsp;</td>
                    <td>
                        <input type="radio" name="oldFileRequestChecksumType" id="oldFileRequestDisableChecksum" value="disabled" checked="yes"/> 
                        <label for="oldFileRequestDisableChecksum">disable</label>
                        <input type="radio" name="oldFileRequestChecksumType" id="oldFileRequestmd5ChecksumType" value="md5"/> 
                        <label for="oldFileRequestmd5ChecksumType">md5</label>
                        <input type="radio" name="oldFileRequestChecksumType" id="oldFileRequestsha1ChecksumType" value="sha1"/> 
                        <label for="oldFileRequestsha1ChecksumType">sha1</label>
                    </td>
                    <td>&nbsp;&nbsp;</td>
                    <td>New file checksum request type: (optional):</td>
                    <td>&nbsp;</td>
                    <td>
                        <input type="radio" name="newFileRequestChecksumType" id="newFileRequestDisableChecksum" value="disabled" checked="yes"/> 
                        <label for="newFileRequestDisableChecksum">disable</label>
                        <input type="radio" name="newFileRequestChecksumType" id="newFileRequestmd5ChecksumType" value="md5"/> 
                        <label for="newFileRequestmd5ChecksumType">md5</label>
                        <input type="radio" name="newFileRequestChecksumType" id="newFileRequestsha1ChecksumType" value="sha1"/> 
                        <label for="newFileRequestsha1ChecksumType">sha1</label>
                    </td>
                </tr>
                <tr>
                    <td>Old file checksum request salt (optional):</td>
                    <td>&nbsp;</td>
                    <td><input type="text" id="oldFileRequestChecksumSalt"/></td>
                    <td>&nbsp;&nbsp;</td>
                    <td>New file checksum requestsalt (optional):</td>
                    <td>&nbsp;</td>
                    <td><input type="text" id="newFileRequestChecksumSalt"/></td>
                </tr>
            </table>
            <input type="submit" value="Replace file"/>
        </form> 

    <div id="status"></div>
        
    <script>
        $("#replaceFileForm").submit(function() {
            var fileID = $("#fileID").val();
            fileID = fileID.replace(/\s+/g, '');
            var pillarID = $("#pillarID").val();
            pillarID = pillarID.replace(/\s+/g, '');
            var fileAddress = $("#fileaddr").val();
            var fileSize = $("#filesize").val();
            var oldFileChecksumVal = $("#oldFileChecksum").val();
            var oldFileChecksumType = $("input[name=oldFileChecksumType]:checked").val();
            var oldFileChecksumSalt = $("#oldFileChecksumSalt").val();
            var oldFileRequestChecksumType = $("input[name=oldFileRequestChecksumType]:checked").val();
            var oldFileRequestChecksumSalt = $("#oldFileRequestChecksumSalt").val(); 
            var newFileChecksumVal = $("#newFileChecksum").val();
            var newFileChecksumType = $("input[name=newFileChecksumType]:checked").val();
            var newFileChecksumSalt = $("#newFileChecksumSalt").val();
            var newFileRequestChecksumType = $("input[name=newFileRequestChecksumType]:checked").val();
            var newFileRequestChecksumSalt = $("#newFileRequestChecksumSalt").val();   	  	
            	
            var command = "repo/reposervice/replaceFile/?fileID=" + fileID + "&pillarID=" + pillarID +
                    "&oldFileChecksum=" + oldFileChecksumVal + "&oldFileChecksumType="+ oldFileChecksumType + 
                    "&oldFileChecksumSalt=" + oldFileChecksumSalt + "&oldFileRequestChecksumType=" + oldFileRequestChecksumSalt + 
                    "&oldFileRequestChecksumSalt=" + oldFileRequestChecksumSalt + "&ufl=" + fileAddress + "&fileSize=" fileSize +
                    "&newFileChecksum=" + newFileChecksumVal + "&newFileChecksumType="+ newFileChecksumType + 
                    "&newFileChecksumSalt=" + newFileChecksumSalt + "&newFileRequestChecksumType=" + newFileRequestChecksumSalt + 
                    "&newFileRequestChecksumSalt=" + newFileRequestChecksumSalt;
            	
            $('#status').load(command).show();
            return true;
        });
    </script> 
        
   	<script>
  		function submit() { return ; }
  	</script>     
    </div>
</html>
