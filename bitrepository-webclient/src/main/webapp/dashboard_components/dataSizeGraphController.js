
  function makeCollectionSelectionCheckboxes(collectionSelector, dsGraph, colorMapper) {
    var graph = dsGraph;
    $(collectionSelector).empty();
    var collections = graph.getCollectionIDs();
    for(i in collections) {
      var elementID = collections[i] + "-selector";
      var color = colorMapper.getCollectionColor(collections[i]);
      checkbox = makeCollectionSelectionCheckbox(collections[i], elementID, color);
      $(collectionSelector).append(checkbox);
      $("#" + elementID).change(function(event) {event.preventDefault(); collectionChanged(event.target.value, event.target.checked, graph);});
    }
  }

  function collectionChanged(collectionID, selected, graph) {
    if(selected) {
      graph.enableCollection(collectionID);
    } else {
      graph.disableCollection(collectionID);
    }
  }

  function makeCollectionSelectionCheckbox(collectionID, elementID, color) {
    var html = "<div class=\"collectionCheckBoxes\"><input type=\"checkbox\" id=\"" + elementID + "\" value=\"" + collectionID + "\" checked>";
    html += "<div class=\"checkboxLegendWrap\"><div class=\"checkboxLegend\" style=\"background-color: " + color + "\"></div></div>" + collectionID + "</div>";
    return html;
  }
