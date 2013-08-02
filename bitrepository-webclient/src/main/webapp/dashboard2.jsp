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
    <link href="css/zoombutton.css" rel="stylesheet" media="screen">
  </head>
  <body>
    <div id="pageMenu"></div>
    <div class="container-fluid">
      <div class="row-fluid">
        <div class="">
          <h2>Overview of your bitpreservation solution</h2>
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
        <div id="dataSizeGraphContainer" class="dataSizeGraph">
          <form class="dashboardForm">
            <select id="graphType">
              <option value="growth" selected>Growth</option>
              <option value="delta">Rate of growth</option>
            </select>
            <div id="dataSizeGraphCollectionSelection"></div>
          </form>
          <div id="dataSizeGraphPlaceholder" style="height:300px;"></div>
        </div>
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
    <script type="text/javascript" src="FileSizeUtils.js"></script>
    <script type="text/javascript" src="momentjs/moment.min.js"></script>
    <script type="text/javascript" src="dashboard_components/ColorMapper.js"></script>
    <script type="text/javascript" src="dashboard_components/CollectionNameMapper.js"></script>
    <script type="text/javascript" src="dashboard_components/collectionStatus.js"></script>
    <script type="text/javascript" src="dashboard_components/legsSizePie.js"></script>
    <script type="text/javascript" src="dashboard_components/collectionSizePie.js"></script>
    <script type="text/javascript" src="dashboard_components/dataSizeGraph.js"></script>
    <script type="text/javascript" src="dashboard_components/dataSizeGraphController.js"></script>

    <script>
      var update_page;
      var update_data_size_graph;
      var colorMapper;
      var nameMapper;
      var dsGraph;

      function init() {
        setIntegrityServiceUrl("<%= su.getIntegrityServiceUrl() %>");
        $.getJSON("repo/reposervice/getCollectionIDs/", {}, function(collections) {
          colorMapper = new ColorMapper(collections);
          nameMapper = new CollectionNameMapper(collections, function(collection, mapper) {updateCheckboxLabel(collection, mapper)});
          setNameMapper(nameMapper);
          loadCollections(collections, "#collectionStatusBody");
          var dataUrl = "<%= su.getIntegrityServiceUrl() %>" + "/integrity/Statistics/getDataSizeHistory/?collectionID=";
          dsGraph = new dataSizeGraph(collections, colorMapper, nameMapper, new FileSizeUtils(), dataUrl, "#graphType", "#dataSizeGraphPlaceholder");
          makeCollectionSelectionCheckboxes("#dataSizeGraphCollectionSelection", dsGraph, colorMapper, nameMapper);
          drawPillarDataSizePieChart("<%= su.getIntegrityServiceUrl() %>" + "/integrity/Statistics/getLatestPillarDataSize/");
          drawCollectionDataSizePieChart("<%= su.getIntegrityServiceUrl() %>" + "/integrity/Statistics/getLatestcollectionDataSize/", colorMapper);
          $("#graphType").change(function(event) {event.preventDefault(); dsGraph.graphTypeChanged();});
          dsGraph.updateData();
          update_page = setInterval(function() {
            refreshContent(); 
          }, 2500);
          // Update data size history graph every 10 minutes
          update_data_size_graph = setInterval(function() {
            dsGraph.updateData();
          }, 600000);
        });
      }

      $(document).ready(function(){
        makeMenu("dashboard2", "#pageMenu");
        init();
      });
    </script>
  </body>
</html>
