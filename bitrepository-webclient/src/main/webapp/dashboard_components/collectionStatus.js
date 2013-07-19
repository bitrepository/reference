



  var collections = new Object();
  

  function loadCollections(url, tableBody) {
    $.getJSON('repo/reposervice/getCollectionIDs/', {}, function(j) {
      for(var i = 0; i < j.length; i++) {
        collections[j[i]] = {collectionID: j[i],
                             collectionName: j[i],
                             numFiles: 0, 
                             latestIngest: "Unknown",
                             collectionSize: 0,
                             pillars: 0,
                             lastCheck: "Unknown",
                             numChecksumErrors: 0,
                             numMissingFiles: 0,
                             nextCheck: "Unknown"};
        
        $(tableBody).append(makeCollectionRow(collections[j[i]]));
      }
    });
  }

  function makeCollectionRow(collection) { 
    var id = collection.collectionID;
    var html = "";
    html += "<tr id=\"" + id + "-row\">";
    html += "<td id=\""+ id + "-name\" class=\"collectionName\">" + id + "</div></td>";
    html += "<td id=\""+ id + "-numFiles\"></td>";
    html += "<td id=\""+ id + "-latestIngest\"></td>";
    html += "<td id=\""+ id + "-collectionSize\"></td>";
    html += "<td id=\""+ id + "-pillars\"></td>";
    html += "<td id=\""+ id + "-latestCheck\"></td>";
    html += "<td id=\""+ id + "-numChecksumErrors\"></td>";
    html += "<td id=\""+ id + "-numMissingFiles\"></td>";
    html += "<td id=\""+ id + "-nextCheck\"></td></tr>";
    return html;
  }

  function updateCollectionRow(collection) {
    var id = collection['collectionID'];
    $("#" + id + "-name").html(collection.collectionName);
    $("#" + id + "-numFiles").html(collection.numFiles);
    $("#" + id + "-latestIngest").html(collection.latestIngest);
    $("#" + id + "-collectionSize").html(collection.collectionSize);
    $("#" + id + "-pillars").html(collection.pillars);
    $("#" + id + "-latestCheck").html(collection.lastCheck);
    $("#" + id + "-numChecksumErrors").html(collection.numChecksumErrors);
    $("#" + id + "-numMissingFiles").html(collection.numMissingFiles);
    $("#" + id + "-nextCheck").html(collection.nextCheck);
  }

  function refreshCollectionStatus(tableBody) {
    for(c in collections) {
      updateCollectionRow(collections[c]);
    }
  }

