function makeCollectionSelectionCheckboxes(collectionSelector, dsGraph, colorMapper, nameMapper) {
    let graph = dsGraph;
    $(collectionSelector).empty();
    let collections = graph.getCollectionIDs();
    for (let i in collections) {
        let elementID = collections[i] + "-selector";
        let color = colorMapper.getCollectionColor(collections[i]);
        let name = nameMapper.getName(collections[i]);
        let checkbox = makeCollectionSelectionCheckbox(collections[i], elementID, color, name);
        $(collectionSelector).append(checkbox);
        $("#" + elementID).on("change", function(event) {event.preventDefault(); collectionChanged(event.target.value, event.target.checked, graph);});
    }
}

function collectionChanged(collectionID, selected, graph) {
    if (selected) {
        graph.enableCollection(collectionID);
    } else {
        graph.disableCollection(collectionID);
    }
}

function makeCollectionSelectionCheckbox(collectionID, elementID, color, name) {
    let html = "<div class=\"collectionCheckBoxes\">";
    html += "<input style=\"margin-right:4px;\" type=\"checkbox\" id=\"" + elementID + "\" value=\"" + collectionID + "\" checked>";
    html += "<div class=\"checkboxLegendWrap\" style=\"margin-right:2px;\">";
    html += "<div class=\"checkboxLegend\" style=\"background-color: " + color + "\"></div></div>";
    html += "<div  style=\"display:inline-block; margin-left:2px;\" id=\"" + elementID + "-name\">" + name + "</div></div>";
    return html;
}

