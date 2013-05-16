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

 <%@ include file="dashboard_components/pop_up_script.jsp"%>

<br>
<table>
<tr>
<td width="600px"> <p style="text-align:left; font-size:28px">Overblik over din bitbevaringsløsning</p>  </td>
<td><a href="#" onclick="openpopup('popup_kontrol')">Læs om procedurer for kontrol</a> </td>
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
 
<div id="popup_kontrol" class="popup"> 
 <%@ include file="dashboard_components/procedurer_for_kontrol.jsp"%>
</div>  

  <!-- Pop-up div -->
 <div id="bg" class="popup_bg"></div> 

  </body>  
 </html>