

  function dataSizeGraph(collections, colorMapper, graphTypeSelector, collectionSelector, graphPlaceholder) {
 
    var collectionIDs = new Object();
    var colerMap = colorMapper;
    var graphType = graphTypeSelector;
    var collectionSelect = collectionSelector;
    var placeholder = graphPlaceholder;
    var graphDataPool = new Object();

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

    this.updateDate = function() {
      // update the cached data
    }

    this.getCollectionIDs = function() {
      return Object.keys(collectionIDs);
    } 
  }


