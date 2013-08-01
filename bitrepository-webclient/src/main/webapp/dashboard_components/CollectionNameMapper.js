
  function CollectionNameMapper(collections) {
    var nameMap = new Object();
    
    for(i=0; i<collections.length; i++) {
      nameMap[collections[i]] = collections[i];
    }

    this.setName = function(collectionID, name)  {
      nameMap[collectionID] = name;
    }
 
    this.getName = function(collectionID) {
      return nameMap[collectionID];
    }
  }
