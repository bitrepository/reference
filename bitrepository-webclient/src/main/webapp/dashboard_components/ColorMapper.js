  

  function ColorMapper(collections) {

    var colorList = new Array("#8cacc6", "#ee1c1c", "#4da74d", "#9440ed", "#ffe84c", "#afd8f8", "#a23c3c",
  	                          "#3d853d", "#7633bd", "#edc240", "#d2ffff", "#f35a5a", "#5cc85c", "#b14cff",
	                          "#ffe84c", "#698194", "#792d2d", "#2e642e", "#58268e", "#8e7426", "#9ce1e1",
        	                  "#ff6969", "#6be96b", "#cf59ff", "#ffff59", "#455663", "#511d1d", "#5e4d19");

    var colorMap = new Object();

    for(i=0; i<collections.length; i++) {
      colorMap[collections[i].collectionID] = colorList[i % colorList.length];
    }

    this.getCollectionColor = function(collectionID) {
      return colorMap[collectionID];
    }
  }
