

  function dataSizeGraph(collections, colorMapper, graphTypeSelector, collectionSelector, graphPlaceholder) {
 
    var collectionsIDs = new Array();
    var colerMap = colorMapper;
    var graphType = graphTypeSelector;
    var collectionSelect = collectionSelector;
    var placeholder = graphPlaceholder;
    var graphDataPool = new Object();

    $(collectionSelect).empty();

    this.collectionChanged = function(element) {
      alert("collection: " + element.val());

    }

    function addCollectionSelectionCheckbox(collectionID) {
      var elementID = collectionID + "-selector";
      var html = "<label class='checkbox inline'>";
      html+= "<input type='checkbox' id='"+ elementID + "' value='" + collectionID + "'> " + collectionID;
      html += "</label>";
      $(collectionSelect).append(html);
      $("#" + elementID).change(function(event) {event.preventDefault(); collectionChanged(this);});
    }

    for(i=0; i<collections.length; i++) {
      collectionIDs[i] = {collectionID : collections[i], state : "active" };
      addCollectionSelectionCheckbox(collections[i]);
    }

    

    this.updateCollectionSelection = function() {
      for(i=0; i<collectionIDs.length; i++) {
        
      }
    }

  }
