
  function CollectionNameMapper(collections) {
    var nameMap = new Object();
    
    for(i=0; i<collections.length; i++) {
      nameMap[collections[i].collectionID] = collections[i].collectionName;
    }

    this.getName = function(collectionID) {
      return nameMap[collectionID];
    }

    this.getCollectionIDs = function() {
      return Object.keys(nameMap);
    }
  }
