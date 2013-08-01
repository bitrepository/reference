

  function dataSizeGraph(collections, colorMapper, nameMapper, dataUrl, graphTypeSelector, graphPlaceholder) {
 
    var collectionIDs = new Object();
    var colerMap = colorMapper;
    var nameMap = nameMapper;
    var graphType = graphTypeSelector;
    var placeholder = graphPlaceholder;
    var graphDataPool = new Object();
    var url = dataUrl;
    var yAxisText = "y-axis text";
    var mySelf = this;

    for(i=0; i<collections.length; i++) {
      collectionIDs[collections[i]] = {state : "active" };
    }

    this.enableCollection = function(collectionID) {
      collectionIDs[collectionID].state = "active";
      this.renderGraph();
    }

    this.disableCollection = function(collectionID) {
      collectionIDs[collectionID].state = "disabled";
      this.renderGraph();
    }

    this.graphTypeChanged = function() {
      //handle change in graph type
      alert("Graph type changed to: " + $(graphType).val());
    }

    function useRange(element, plot, dataObj, options) {
      $(this).bind("plotselected", function (event,ranges) {
        // do the zooming
        plot = $.plot($(element), 
                      dataObj,
                      $.extend(true,
                               {},
                               options,
                               {
                                xaxis: { min: ranges.xaxis.from, max: ranges.xaxis.to },   
                                yaxis: { min: ranges.yaxis.from, max: ranges.yaxis.to }
                               }
                              )
                     );
        });
    }

    this.renderGraph = function() {
      var dataObj = new Array();
      for(i in collectionIDs) {
        var collectionID = i;
        if(collectionIDs[i].state == "active" && graphDataPool[collectionID] != null) {
          var dataArray = graphDataPool[collectionID].slice();
          var collectionObj = {label: nameMap.getName(collectionID), data: dataArray, color: colorMapper.getCollectionColor(collectionID)};
          dataObj.push(collectionObj);
        }
      }
      
      var options = {
        hoverable: true,
        legend:{        
            backgroundOpacity: 0.5,
            noColumns: 5,  
            position: "nw"
        },
        grid:  {
            hoverable: true,
            borderColor: "#cccccc"
        },
        xaxis: {  mode: "time",  localTimezone: true , zoomRange: [0.1, 10] , timeformat: "%y/%0m/%0d %0H:%0M"},
        yaxis: {  axisLabel: yAxisText},
        selection:{  mode: "xy" } ,
        points: { show: true ,  radius: 1} ,
        lines: { show: true},
        zoom: { interactive: true}
      };
    
      var plot = $.plot(placeholder, dataObj, options);
      useRange(placeholder, plot, dataObj, options);

      $('<div class="button" style="left:600px;top:20px">zoom out</div>').appendTo(placeholder).click(function (e) {
          e.preventDefault();
          plot.setupGrid();
          plot.draw();
          plot = $.plot(placeholder, dataObj, options);
      });

      // use graph type to render the type of graph, i.e. growth or rate of growth    
    }

    this.getGraphData = function() {
      return graphDataPool;
    }

    function updateCollectionData(collection) {
      var c = collection;
      $.getJSON(url + c, {}, function(data) {
          collectionData = new Array();
          for(i=0; i<data.length; i++) {
            a = new Array(data[i].dateMillis, data[i].dataSize);
            collectionData[i] = a;
          }
          graphDataPool[c] = collectionData;
        }).done(function() {mySelf.renderGraph()});
    }

    this.updateData = function() {
      var keys = Object.keys(collectionIDs);
      for(i in keys) {
        updateCollectionData(keys[i]);
      }
    }

    this.getCollectionIDs = function() {
      return Object.keys(collectionIDs);
    } 
  }


