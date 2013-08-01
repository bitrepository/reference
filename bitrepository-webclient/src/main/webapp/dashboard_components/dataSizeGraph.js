

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

    this.renderGraph = function(graphType) {
      // use graph type to render the type of graph, i.e. growth or rate of growth    
    }

    this.getGraphData = function() {
      return graphDataPool;
    }

    this.updateData = function() {
      for(i in Object.keys(collectionIDs)) {
        var collection = collectionIDs[i];
        $.getJSON(url + collection, {}, function(data) {
          data = new Array();
          for(i=0; i<data.length; i++) {
            a = new Array(data[i].dateMillis, data[i].dataSize);
            data[i] = a;
          }
          graphDataPool[collection] = data;
        });
      }
    }

    this.getCollectionIDs = function() {
      return Object.keys(collectionIDs);
    } 
  }


