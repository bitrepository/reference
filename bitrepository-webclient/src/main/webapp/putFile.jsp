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
<script type="text/javascript" src="functions.js"></script>

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
                    <td> <input type="text" class="inputURL" id="putFileaddr" value="<%= su.getDefaultHttpServerUrl() %>"/></td>
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
                        <input type="radio" name="putChecksumType" id="putmd5ChecksumType" value="MD5" checked="yes"/> 
                        <label for="putmd5ChecksumType">MD5</label>
                        <input type="radio" name="putChecksumType" id="putsha1ChecksumType" value="SHA1"/> 
                        <label for="putsha1ChecksumType">SHA1</label>
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
                        <input type="radio" name="approveChecksumType" id="approvemd5ChecksumType" value="MD5"/> 
                        <label for="approvemd5ChecksumType">MD5</label>
                        <input type="radio" name="approveChecksumType" id="approvesha1ChecksumType" value="SHA1"/> 
                        <label for="approvesha1ChecksumType">SHA1</label>
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
    <div id="putMessagediv"></div>
        
    <script>
        $("#putFileForm").submit(function() {
            //$('#putMessagediv').load("").show();
            var fileName = $("#putFileForm").find("#putFilename").val();
            var fileAddr = $("#putFileForm").find("#putFileaddr").val();
            var fileSize = $("#putFileForm").find("#putFilesize").val();
            var verifyChecksumVal = $("#putFileForm").find("#putChecksum").val();
            var verifyChecksumType = $("#putFileForm").find("input[name=putChecksumType]:checked").val();
            var verifyChecksumSalt = $("#putFileForm").find("#putChecksumSalt").val();
            var approveChecksumType = $("#putFileForm").find("input[name=approveChecksumType]:checked").val();
            var approveChecksumSalt = $("#putFileForm").find("#approveChecksumSalt").val();      
    
            if (fileName == "") {
                //$('#putMessagediv').html("<p2>Invalid filename!</p2>").show();
                return false;
            }
            if (fileAddr == "") {
                //$('#putMessagediv').html("<p2>Invalid address!</p2>").show();
                return false;
            }
            if(!is_int(fileSize)) {
                $('#putMessagediv').html("<p2>Invalid filesize!</p2>").show();
                return false;
            }
            if(fileSize == "") {
                //$('#putMessagediv').html("<p2>Invalid filesize!</p2>").show();
                return false;
            }
            if(approveChecksumSalt == "disabled") {
            	approveChecksumSalt = "";
            }
            var command = "repo/reposervice/putfile/?fileID=" + fileName + "&url=" + fileAddr + "&fileSize=" + fileSize +
            		"&putChecksum=" + verifyChecksumVal + "&putChecksumType=" + verifyChecksumType + "&putSalt=" + 
            		verifyChecksumSalt + "&approveChecksumType=" + approveChecksumType + "&approveSalt=" + 
            		approveChecksumSalt;
            $('#putMessagediv').load(command, function(response, status, xhr) {
                if (status == "error") {
                    $("#putMessagediv").html(response);
                }
            }).show();
            return true;
        });
    </script>   
    
    <script>
        function submit() { return ; }
    </script>    
</html>
