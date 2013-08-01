

  function dataSizeGraph(collections, colorMapper, dataUrl, graphTypeSelector, graphPlaceholder) {
 
    var collectionIDs = new Object();
    var colerMap = colorMapper;
    var graphType = graphTypeSelector;
    var placeholder = graphPlaceholder;
    var graphDataPool = new Object();
    var url = dataUrl;

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
      alert("Graph type changed to: " + graphType.val())
    }

    this.renderGraph = function(graphType) {
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
        });
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


