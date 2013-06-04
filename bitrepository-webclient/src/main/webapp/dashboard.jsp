<%--
  #%L
  Bitrepository Webclient
  %%
  Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
<%@page pageEncoding="UTF-8"%>
<%@page import="java.util.*,
        org.bitrepository.common.utils.FileSizeUtils,
        org.bitrepository.dashboard.*,
        org.bitrepository.common.webobjects.*" %>

<!DOCTYPE html>
<html>
  <head>
    <title>Bitrepository dashboard</title>
    <link href="bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">    
    <meta http-equiv="refresh" content="300">
  </head>
  <body>
  <div id="pageMenu"></div>
<!-- Javascript -->
<script src="jquery/jquery-1.9.0.min.js"></script>
<script src="bootstrap/js/bootstrap.min.js"></script>
<script src="flot/excanvas.min.js"></script>
<script src="flot/jquery.flot.min.js"></script>
<script src="flot/jquery.flot.pie.js"></script>
<script src="flot/jquery.flot.selection.min.js"></script>
<script src="flot/jquery.flot.axislabels.js"></script>

<script type="text/javascript" src="menu.js"></script>

<script>
    $(document).ready(function(){
      makeMenu("dashboardServlet", "#pageMenu");
     }); 
</script>

<br>
<table>
<tr>
<td width="600px"> <p style="text-align:left; font-size:28px">Overblik over din bitbevaringsløsning</p>  </td>
<td><a href="#myModal" role="button" data-toggle="modal">Procedurer for kontrol</a></td>      
</tr>
</table>

 <table  cellpadding="5px" cellspacing="5px">
  <tr>
    <td width ="100%">
       <table style="cellpadding="10px">
          <tr>
          <td><%@ include file="dashboard_components/collections_status.jsp"%></td>
         </tr>
       </table>
    </td>
   </tr>
 </table>

 <table  cellpadding="5px" cellspacing="5px">
  <tr>
    <td width ="100%">
       <table  style="border:3px solid grey;"  cellpadding="15px">       
         <tr>
          <td><%@ include file="dashboard_components/data_size_graph.jsp"%></td>
         </tr>
       </table>
    </td>
   </tr>
</table>
    
<table cellpadding="5px" cellspacing="5px">
  <tr>
    <td width ="50%">
       <table style="border:3px solid grey;" cellpadding="10px" >
         <tr>
          <td> <%@ include file="dashboard_components/collections_size_pie.jsp"%> </td>
         </tr>
       </table>
    </td>
   <td width="50%">
     <table style="border:3px solid grey;" cellpadding="10px">
         <tr>
          <td>  <%@ include file="dashboard_components/legs_size_pie.jsp"%></td>
         </tr>
     </table>
   </td>
</tr>
</table>
 
<!-- Modal -->
<div id="myModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-header">
  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
  <h3 id="myModalLabel">Procedurer for kontrol</h3>
  </div>
  <div class="modal-body">
  <p><%@ include file="dashboard_components/procedure_for_control_text.jsp"%></p>
  </div>
  <div class="modal-footer">
  <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
  </div>
</div>
 

 </html>