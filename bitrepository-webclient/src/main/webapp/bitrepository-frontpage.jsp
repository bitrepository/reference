<%@page import="org.bitrepository.webservice.ServiceUrlFactory" %>
<%@page import="org.bitrepository.webservice.ServiceUrl" %>
<!DOCTYPE html>
<html>
  <% ServiceUrl su = ServiceUrlFactory.getInstance(); %>
  <head>
    <title>Bitrepository frontpage</title>
    <link href="bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">
  </head>
  <body>
  
  <div id="pageMenu"></div>
  <div class="container-fluid">
    <div class="row-fluid">
      <div class="span9"> 
        <div class="span9" style="height:0px; min-height:0px"></div>
        <div class="span9"><h2>Welcome</h2></div>
        <div class="span9">
           Welcome to the Bitrepository services frontpage. <br>
           Please move on the the specific service pages. <br>
           The services are: 
           <ul>
             <li><a href="alarm-service.jsp">Alarm</a></li>
             <li><a href="integrity-service.jsp">Integrity</a></li>
             <li><a href="audit-trail-service.jsp">Audit trail</a></li>
             <li><a href="status-service.jsp">Status</a></li>
           </ul>

           To perform actions on the content in the repository, use the commandline clients. 
        </div>
      </div>
    </div>
  </div>
  <script type="text/javascript" src="jquery/jquery-1.9.0.min.js"></script>
  <script type="text/javascript" src="bootstrap/js/bootstrap.min.js"></script>
  <script type="text/javascript" src="menu.js"></script>

  <script>
        
    $(document).ready(function(){
      makeMenu("bitrepository-frontpage.jsp", "#pageMenu");
    }); 

    </script>
  </body>
</html>
