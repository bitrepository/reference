

  var collections = new Object();
  var readyForRefresh = false;
  var integrityServiceUrl;
  var myNameMapper;
  var update_page;
  var updateLock;

  function setIntegrityServiceUrl(url) {
    integrityServiceUrl = url;
  }

  function setNameMapper(nameMapper) {
    myNameMapper = nameMapper;
  }

  function initiateCollectionStatus(collectionIDs, tableBody, updateInterval) {
    for(var i = 0; i < collectionIDs.length; i++) {
        collections[collectionIDs[i].collectionID] = {collectionID: collectionIDs[i].collectionID,
                                         collectionName: collectionIDs[i].collectionName,
                                         numFiles: '-', 
                                         latestIngest: "Fetching",
                                         collectionSize: '-',
                                         pillars: '-',
                                         lastCheck: "Fetching",
                                         numChecksumErrors: '-',
                                         numMissingFiles: '-',
                                         nextCheck: "Fetching"};
        $(tableBody).append(makeCollectionRow(collections[collectionIDs[i].collectionID]));
      }
      readyForRefresh = true;
      initStatusUpdate(updateInterval);
  }

  function updateWorkflowStatus(collection) {
    if(scheduleUpdate(collection, "status")) {
      url = integrityServiceUrl + "/integrity/IntegrityService/getWorkflowSetup/?collectionID=" + collection;
      var c = collection;
      $.getJSON(url, {}, function(j) {
        collections[c].lastCheck = j[0].lastRun;
        collections[c].nextCheck = j[0].nextRun;
      }).done(function(){updateCollectionRow(collections[c]);}).always(function() {finishUpdate(c, "status");});
    }
  }

  function updateWorkflowStatuses() {
    if(readyForRefresh) {
      for(c in collections) {
        updateWorkflowStatus(c);
      }
    }
  }

  function updateCollectionInfo(collection) {
    if(scheduleUpdate(collection, "info")) {
      url = integrityServiceUrl + "/integrity/IntegrityService/getCollectionInformation/?collectionID=" + collection;
      var c = collection;
      $.getJSON(url, {}, function(j) {
        collections[c].numFiles = j.numberOfFiles;
        collections[c].collectionSize = j.collectionSize;
        collections[c].latestIngest = j.lastIngest;
      }).done(function(){updateCollectionRow(collections[c]);}).always(function() {finishUpdate(c, "info");});
    }
  }

  function updateInfo() {
    if(readyForRefresh) {
      for(c in collections) {
        updateCollectionInfo(c);
      }
    }
  }

  function updateCollectionStatistic(collection) { 
    if(scheduleUpdate(collection, "stats")) {
      url = integrityServiceUrl + "/integrity/IntegrityService/getIntegrityStatus/?collectionID=" + collection;
      var c = collection;
      $.getJSON(url, {}, function(j) {
        var checksumErrors = 0;
        var missingFiles = 0;
        var pillarCount = 0;
        for(stat in j) {
          pillarCount += 1;
          checksumErrors += j[stat].checksumErrorCount;
          missingFiles += j[stat].missingFilesCount;
        }
        collections[c].pillars = pillarCount;
        collections[c].numChecksumErrors = checksumErrors;
        collections[c].numMissingFiles = missingFiles;
      }).done(function(){updateCollectionRow(collections[c])}).always(function() {finishUpdate(c, "stats");}); 
    }
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
    var id = collection.collectionID;
    $("#" + id + "-name").html(collection.collectionName);
    $("#" + id + "-numFiles").html(collection.numFiles);
    $("#" + id + "-latestIngest").html(collection.latestIngest);
    $("#" + id + "-collectionSize").html(collection.collectionSize);
    $("#" + id + "-pillars").html(collection.pillars);
    $("#" + id + "-latestCheck").html(collection.lastCheck);
    $("#" + id + "-numChecksumErrors").html(collection.numChecksumErrors);
    if(collection.numChecksumErrors > 0) {
      $("#" + id + "-numChecksumErrors").addClass("badge");
      $("#" + id + "-numChecksumErrors").addClass("badge-important");
      $("#" + id + "-numChecksumErrors").addClass("pull-right");
    } else {
      $("#" + id + "-numChecksumErrors").removeClass("badge");
      $("#" + id + "-numChecksumErrors").removeClass("badge-important");
      $("#" + id + "-numChecksumErrors").removeClass("pull-right");
    }
    $("#" + id + "-numMissingFiles").html(collection.numMissingFiles);
    if(collection.numMissingFiles > 0) {
      $("#" + id + "-numChecksumErrors").addClass("badge");
      $("#" + id + "-numChecksumErrors").addClass("badge-important");
      $("#" + id + "-numChecksumErrors").addClass("pull-right");
    } else {
      $("#" + id + "-numChecksumErrors").removeClass("badge");
      $("#" + id + "-numChecksumErrors").removeClass("badge-important");
      $("#" + id + "-numChecksumErrors").removeClass("pull-right");
    }
    $("#" + id + "-nextCheck").html(collection.nextCheck);
  }

  function refreshContent() {
      updateWorkflowStatuses();
      updateInfo();
      updateStatistics();
  }

  function scheduleUpdate(collection, type) {
    if(updateLock[collection][type]) {
      updateLock[collection][type] = false;
      return true;
    } else {
      return false;
    }
  }
  
  function finishUpdate(collection, type) {
    return updateLock[collection][type] = true;
  }

  function initStatusUpdate(updateInterval) {
    // Init update locks
    updateLock = new Array();
    for(c in collections) {
      var row = new Array();
      row["status"] = true;
      row["info"] = true;
      row["stats"] = true;
      updateLock[c] = row;
    }
    refreshContent();
    update_page = setInterval(function() { refreshContent(); }, updateInterval);
  }



