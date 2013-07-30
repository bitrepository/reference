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
<%@page import="org.bitrepository.webservice.ServiceUrlFactory" %>
<%@page import="org.bitrepository.webservice.ServiceUrl" %>
<!DOCTYPE html>
<html>
  <% ServiceUrl su = ServiceUrlFactory.getInstance(); %>
  <head>
    <title>Bitrepository dashboard</title>
    <link href="bootstrap/css/bootstrap.min.css" rel="stylesheet" media="screen">
    <link href="css/dashboard.css" rel="stylesheet" media="screen">
  </head>
  <body>
    <div id="pageMenu"></div>
    <div class="container-fluid">
      <div class="row-fluid">
        <div class="">
          <h2>Overview of your bitpresevation solution</h2>
        </div>
        <div class="collectionStatus" id="statusDiv">
          <table class="table table-hover table-condensed">
            <thead>
              <tr>
                <th class="collectionName">Collection name</th>
                <th>Number of files</th>
                <th>Latest Ingest</th>
                <th>Size of collection</th>
                <th>Pillars</th>
                <th>Latest check</th>
                <th>Number of checksum errors</th>
                <th>Number of missing files</th>
                <th>Next scheduled check</th>
              </tr>
            </thead>
            <tbody id="collectionStatusBody"></tbody>
          </table>
        </div>
        <div id="dataSizeGraphContainer" class="dataSizeGraph"></div>
        <div id="collectionPieBoxContainer" class="collectionPieBox">
          <h3>Data distributed on collections</h3>
          <div class="collectionPie">
            <div id="collection">
              <div id="flotcontainer_collection" style="width: 280px;height:280px; text-align: left;"></div>
            </div>
          </div>
          <div id=collectionLegendDiv></div>
        </div>
        <div id="legPieBoxContainer" class="legPieBox">
          <h3>Data distributed on pillars</h3>
          <div class="legPie">
            <div id="data_pillar">
              <div id="flotcontainer_data_pillar" style="width: 280px;height:280px; text-align: left;"></div>
            </div>
          </div>
          <div id=pillarLegendDiv></div>
        </div>
      </div>
    </div>

    <!-- Javascript -->
    <script src="jquery/jquery-1.9.0.min.js"></script>
    <script src="bootstrap/js/bootstrap.min.js"></script>
    <script src="flot/excanvas.js"></script>     
    <script src="flot/jquery.flot.min.js"></script>
    <script src="flot/jquery.flot.pie.js"></script>
    <script src="flot/jquery.flot.selection.min.js"></script>
    <script src="flot/jquery.flot.axislabels.js"></script>
    <script src="flot/jquery.flot.resize.js"></script>

    <script type="text/javascript" src="menu.js"></script>
    <script type="text/javascript" src="dashboard_components/collectionStatus.js"></script>
    <script type="text/javascript" src="dashboard_components/legsSizePie.js"></script>
    <script type="text/javascript" src="dashboard_components/collectionSizePie.js"></script>

    <script>
      var update_page;
      var colorMapper;

      function init() {
        setIntegrityServiceUrl("<%= su.getIntegrityServiceUrl() %>");
        $.getJSON("repo/reposervice/getCollectionIDs/", {}, function(collections) {
          colorMapper = new ColorMapper(collections);
          loadCollections(collections, "#collectionStatusBody");
          update_page = setInterval(function() {
            refreshContent(); 
          }, 2500);
          drawPillarDataSizePieChart("<%= su.getIntegrityServiceUrl() %>" + "/integrity/Statistics/getLatestPillarDataSize/");
          drawCollectionDataSizePieChart("<%= su.getIntegrityServiceUrl() %>" + "/integrity/Statistics/getLatestcollectionDataSize/", colorMapper);
        });
      }

      $(document).ready(function(){
        makeMenu("dashboard2", "#pageMenu");
        init();
      });
    </script>
  </body>
</html>
