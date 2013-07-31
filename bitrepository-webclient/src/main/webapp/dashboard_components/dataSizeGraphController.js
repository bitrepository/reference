
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
