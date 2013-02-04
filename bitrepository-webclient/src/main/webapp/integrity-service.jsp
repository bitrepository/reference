<%@page import="org.bitrepository.webservice.ServiceUrlFactory" %>
<%@page import="org.bitrepository.webservice.ServiceUrl" %>
<!DOCTYPE html>
<html>
  <% ServiceUrl su = ServiceUrlFactory.getInstance(); %>
  <head>
    <title>Bitrepository integrity service</title>
    <!-- Bootstrap -->
    <link href="bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">
  </head>
  <body>
  
  <div id="pageMenu"></div>
  <div class="container">
    <div class="row">
      <div class="span9"><h2>Integrity service</h2></div>
      <div class="span9"><h>Not implemented yet</h></div>
    </div>
  </div>
  <script type="text/javascript" src="jquery/jquery-1.9.0.min.js"></script>
  <script type="text/javascript" src="bootstrap/js/bootstrap.min.js"></script>
  <script type="text/javascript" src="menu.js"></script>

  <script>
    $(document).ready(function(){
      makeMenu("integrity-service.jsp", "#pageMenu");
    }); 

    </script>
  </body>
</html>
