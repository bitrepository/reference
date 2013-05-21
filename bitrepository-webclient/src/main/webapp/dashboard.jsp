<%@page pageEncoding="UTF-8"%>
<%@page import="java.util.*,
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
<script src="flot/jquery.flot.pie.min.js"></script>
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
          <td><%@ include file="dashboard_components/samlinger_status.jsp"%></td>
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
          <td><%@ include file="dashboard_components/tilvaekst.jsp"%></td>
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
          <td> <%@ include file="dashboard_components/samlinger_tb.jsp"%> </td>
         </tr>
       </table>
    </td>
   <td width="50%">
     <table style="border:3px solid grey;" cellpadding="10px">
         <tr>
          <td>  <%@ include file="dashboard_components/data_ben_tb.jsp"%></td>
         </tr>
     </table>
   </td>
</tr>
</table>
 
 <<!-- Modal -->
<div id="myModal" class="modal hide fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
  <div class="modal-header">
  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
  <h3 id="myModalLabel">Procedurer for kontrol</h3>
  </div>
  <div class="modal-body">
  <p><%@ include file="dashboard_components/procedurer_for_kontrol.jsp"%></p>
  </div>
  <div class="modal-footer">
  <button class="btn" data-dismiss="modal" aria-hidden="true">Close</button>
  </div>
</div>
 

 </html>