

  var collections = new Object();
  var readyForRefresh = false;
  var integrityServiceUrl;

  function setIntegrityServiceUrl(url) {
    integrityServiceUrl = url;
  }

  function loadCollections(url, tableBody) {
    $.getJSON(url, {}, function(j) {
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
      readyForRefresh = true;
    });
  }

  function loadCollectionName(collection) {
    url = "repo/reposervice/getCollectionName/?collectionID=" + collection;
    var c = collection;
    $.getJSON(url, {}, function(j) {
      collections[c].collectionName = j;
    }, "html");
  }

  function loadCollectionNames() {
    if(readyForRefresh) {
      for(c in collections) {
        loadCollectionName(c);
      }
    }
  }

  function updateWorkflowStatus(collection) {
    url = integrityServiceUrl + "/integrity/IntegrityService/getWorkflowSetup/?collectionID=" + collection;
    var c = collection;
    $.getJSON(url, {}, function(j) {
      collections[c].lastCheck = j[0].lastRun;
      collections[c].nextCheck = j[0].nextRun;
    });
  }

  function updateWorkflowStatuses() {
    if(readyForRefresh) {
      for(c in collections) {
        updateWorkflowStatus(c);
      }
    }
  }

  function updateCollectionStatistic(collection) {
    url = integrityServiceUrl + "/integrity/IntegrityService/getCollectionInformation/?collectionID=" + collection;
    var c = collection;
    $.getJSON(url, {}, function(j) {
      collections[c].numFiles = j.numberOfFiles;
      collections[c].collectionSize = j.collectionSize;
      collections[c].latestIngest = j.lastIngest;
    });
  }

  function updateStatistics() {
    if(readyForRefresh) {
      for(c in collections) {
        updateCollectionStatistic(c);
      }
    }
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

  function refreshCollectionStatus() {
    if(readyForRefresh) {
      for(c in collections) {
        updateCollectionRow(collections[c]);
      }
    }
  }

