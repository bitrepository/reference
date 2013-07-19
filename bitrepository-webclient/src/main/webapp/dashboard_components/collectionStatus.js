

<script>

  var collections = new Object();
  

  function loadCollections(url) {
    $.getJSON('repo/reposervice/getCollectionIDs/', {}, function(j) {
      for(var i = 0; i < j.length; i++) {
        collections[j[i]] = {collectionName: j[i],
                             numFiles: 0, 
                             latestIngest: "Unknown",
                             collectionSize: 0,
                             pillars: 0,
                             lastCheck: "Unknown",
                             numChecksumErrors: 0,
                             numMissingFiles: 0,
                             nextCheck: "Unknown"}
      }
        
    });
  }

</script>
