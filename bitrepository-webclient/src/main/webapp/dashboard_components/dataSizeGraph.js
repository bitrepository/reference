

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


  var graph;

  function makeCollectionSelectionCheckboxes(collectionSelector, dsGraph) {
    var graph = dsGraph;
    $(collectionSelector).empty();
    for(c in graph.getCollectionIDs()) {
      var elementID = collectionID + "-selector";
      makeCollectionSelectionCheckbox(c, elementID);
      $(collectionSelector).append(html);
      $("#" + elementID).change(function(event) {event.preventDefault(); collectionChanged(event.target.value, event.target.checked, graph);});
    }
  }

  function collectionChanged(collectionID, selected, graph) {
    alert("collection: " + collectionID + "changed.");
  }

  function makeCollectionSelectionCheckbox(collectionID, elementID) {
    var html = "<label class='checkbox inline'>";
    html+= "<input type='checkbox' id='"+ elementID + "' value='" + collectionID + "'> " + collectionID;
    html += "</label>";
    return html;
  }

