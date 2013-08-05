
  function makeCollectionSelectionCheckboxes(collectionSelector, dsGraph, colorMapper, nameMapper) {
    var graph = dsGraph;
    $(collectionSelector).empty();
    var collections = graph.getCollectionIDs();
    for(i in collections) {
      var elementID = collections[i] + "-selector";
      var color = colorMapper.getCollectionColor(collections[i]);
      var name = nameMapper.getName(collections[i]);
      checkbox = makeCollectionSelectionCheckbox(collections[i], elementID, color, name);
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

  function makeCollectionSelectionCheckbox(collectionID, elementID, color, name) {
    var html = "<div class=\"collectionCheckBoxes\">";
    html += "<input style=\"margin-right:4px;\" type=\"checkbox\" id=\"" + elementID + "\" value=\"" + collectionID + "\" checked>";
    html += "<div class=\"checkboxLegendWrap\" style=\"margin-right:2px;\">";
    html += "<div class=\"checkboxLegend\" style=\"background-color: " + color + "\"></div></div>";
    html += "<div  style=\"display:inline-block; margin-left:2px;\" id=\"" + elementID + "-name\">" + name + "</div></div>";
    return html;
  }

  function updateCheckboxLabel(collectionID, nameMapper) {
    var elementID = "#" + collectionID + "-selector-name";
    $(elementID).html(nameMapper.getName(collectionID));
  }
