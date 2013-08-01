
  function CollectionNameMapper(collections, callback) {
    var updateCallback = callback;
    var nameMap = new Object();
    
    for(i=0; i<collections.length; i++) {
      nameMap[collections[i]] = collections[i];
    }

    this.setName = function(collectionID, name)  {
      nameMap[collectionID] = name;
      updateCallback(collectionID, this);
    }
 
    this.getName = function(collectionID) {
      return nameMap[collectionID];
    }
  }
