

  function dataSizeGraph(collections, colorMapper, dataUrl, graphTypeSelector, graphPlaceholder) {
 
    var collectionIDs = new Object();
    var colerMap = colorMapper;
    var graphType = graphTypeSelector;
    var placeholder = graphPlaceholder;
    var graphDataPool = new Object();
    var url = dataUrl;
    var yAxisText = "y-axis text";

    for(i=0; i<collections.length; i++) {
      collectionIDs[collections[i]] = {state : "active" };
    }

    this.enableCollection = function(collectionID) {
      collectionIDs[collectionID].state = "active";
    }

    this.disableCollection = function(collectionID) {
      collectionIDs[collectionID].state = "disabled";
    }

    this.graphTypeChanged = function() {
      //handle change in graph type
      alert("Graph type changed to: " + $(graphType).val())
    }

    this.renderGraph = function() {
      var dataObj = new Array();
      for(i in collectionIDs) {
        if(collectionIDs[i].state == "active") {
          var collectionID = collectionIDs[i];
          var dataArray = graphDataPool[collectionID].slice();
          var collectionObj = {label: collectionID, data: dataArray, color: colorMapper.getCollectionColor(collectionID)};
          dataObj.push(collectionObj);
        }
      }
      
      var options = {
        hoverable: true,
        legend:{        
            backgroundOpacity: 0.5,
            noColumns: 5,  
            position: "nw"
        },
        grid:  {
            hoverable: true,
            borderColor: "#cccccc"
        },
        xaxis: {  mode: "time",  localTimezone: true , zoomRange: [0.1, 10] , timeformat: "%y/%0m/%0d %0H:%0M"},
        yaxis: {  axisLabel: yAxisText},
        selection:{  mode: "xy" } ,
        points: { show: true ,  radius: 1} ,
        lines: { show: true},
        zoom: { interactive: true}
      };
    
    var plot = $.plot(placeholder, dataObj, options);

      // use graph type to render the type of graph, i.e. growth or rate of growth    
    }

    this.getGraphData = function() {
      return graphDataPool;
    }

    function updateCollectionData(collection) {
      var c = collection;
      $.getJSON(url + c, {}, function(data) {
          collectionData = new Array();
          for(i=0; i<data.length; i++) {
            a = new Array(data[i].dateMillis, data[i].dataSize);
            collectionData[i] = a;
          }
          graphDataPool[c] = collectionData;
        }).done(function() {renderGraph()});
    }

    this.updateData = function() {
      var keys = Object.keys(collectionIDs);
      for(i in keys) {
        updateCollectionData(keys[i]);
      }
    }

    this.getCollectionIDs = function() {
      return Object.keys(collectionIDs);
    } 
  }


