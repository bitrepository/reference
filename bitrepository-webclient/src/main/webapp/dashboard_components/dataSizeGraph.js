

  function dataSizeGraph(collections, colorMapper, nameMapper, fileSizeUtils, dataUrl, graphTypeSelector, graphPlaceholder) {
 
    var collectionIDs = new Object();
    var colerMap = colorMapper;
    var nameMap = nameMapper;
    var sizeUtils = fileSizeUtils;
    var graphType = graphTypeSelector;
    var placeholder = graphPlaceholder;
    var graphDataPool = new Object();
    var url = dataUrl;
    var yAxisText = "y-axis text";
    var mySelf = this;
    var msPerDay = 86400 * 1000;

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
      this.renderGraph();
    }

    function showTooltip(x, y, contents) {
        $('<div id="tooltip">' + contents + '</div>').css({
            position: 'absolute',
            display: 'none',
            top: y + 5,
            left: x + 20,
            border: '2px solid #4572A7',
            padding: '2px',
            size: '10',
            'border-radius': '6px 6px 6px 6px',
            'background-color': '#fff',
            opacity: 0.80
        }).appendTo("body").fadeIn(200);
    }

    function useRange(element, plot, dataObj, options) {
      $(element).bind("plotselected", function (event,ranges) {
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

    function handleHover(element, plot, dataObj, options) {
      $(element).bind("plothover", function (event, pos, item) {
        if(item) {
          if(previousPoint != item.dataIndex) {
            previousPoint = item.dataIndex;
            $("#tooltip").remove();
            var x = item.datapoint[0];
            var y = item.datapoint[1];
            var formated_date = moment.utc(x).format("YYYY/MM/DD HH:mm");
            showTooltip(item.pageX, 
                        item.pageY,
                        formated_date  + "<br/><strong>" + y.toFixed(4) + " " + yAxisText +  "</strong>");
          }
        } else {
          $("#tooltip").remove();
          previousPoint = null;
        }
        $('<div class="button" style="left:600px;top:20px">zoom out</div>').appendTo(element).click(function (e) {
            e.preventDefault();
            plot.setupGrid();
            plot.draw();
            plot = $.plot(placeholder, dataObj, options);
        });
      });
    }

    function scaleAndCopyData(data, factor) {
      scaledData = new Array();
      for(i=0; i<data.length; i++) {
        scaledData[i] = [data[i][0], data[i][1]/factor];
      }
      return scaledData;
    }

    this.renderGraph = function() {
      var dataObj = new Array();
      var dMax = 0;
      var dataField;
      var dMaxField;
      if($(graphType).val() == "growth") {
        dataField = "data";
        dMaxField = "dataMax";
      } else if($(graphType).val() == "delta") {
        dataField = "deltaData";
        dMaxField = "deltaMax";       
      } else if($(graphType).val() == "filecount") {
        dataField = "fileCount";
        dMaxField = "fileCountMax";       
      }

      for(i in collectionIDs) {
        var collectionID = i;
        if(collectionIDs[i].state == "active" && graphDataPool[collectionID] != null) {
          if(graphDataPool[collectionID][dMaxField] > dMax) {
            dMax = graphDataPool[collectionID][dMaxField];
          }
        }
      }
  
      var unitSuffix = sizeUtils.toHumanUnit(dMax);
      var scale = sizeUtils.getByteSize(unitSuffix);
      
      for(i in collectionIDs) {
        var collectionID = i;
        if(collectionIDs[i].state == "active" && graphDataPool[collectionID] != null) {
          var dataArray          ;
          //Avoid scaling y-axis data when working with filecount.
          if($(graphType).val() == "filecount") {
            dataArray = graphDataPool[collectionID][dataField];
          } else {
            dataArray = scaleAndCopyData(graphDataPool[collectionID][dataField], scale);          
          }

          var collectionObj = {data: dataArray, color: colorMapper.getCollectionColor(collectionID)};
          dataObj.push(collectionObj);
        }
      }

      if($(graphType).val() == "growth") {
        yAxisText = unitSuffix;
      } else if($(graphType).val() == "delta") {
        yAxisText = unitSuffix + " per day";
      } else if($(graphType).val() == "filecount") {
        yAxisText = unitSuffix.substr(0,1) + " files";
      }

      var options = {
        hoverable: true,
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
      handleHover(placeholder, plot, dataObj, options);

    }

    this.getGraphData = function() {
      return graphDataPool;
    }

    function updateCollectionData(collection) {
      var c = collection;
      $.getJSON(url + c, {}, function(data) {
          var collectionData = new Array();
          var deltaCollectionData = new Array();
          var dMax = 0;
          var deltaDataMax = 0;
          var fileCountData = new Array();
          var fileCountDataMax = 0;
          // Get the timezone offset in milliseconds
          var timeOffset = moment().tz("Europe/Copenhagen")._offset * 1000 * 60;
          for(i=0; i<data.length; i++) {
            var a = new Array(data[i].dateMillis - timeOffset, data[i].dataSize);
            if(data[i].dataSize > dMax) {
              dMax = data[i].dataSize;
            }
            collectionData.push(a);

            if(i == 0) {
              deltaCollectionData.push([data[i].dateMillis - timeOffset, 0]);
            } else {
              var deltaBytes = data[i].dataSize - data[i-1].dataSize;
              var deltaMs = data[i].dateMillis - data[i-1].dateMillis;
              var growthPerDay = (msPerDay * deltaBytes) / deltaMs;
              if(growthPerDay > deltaDataMax) {
                deltaDataMax = growthPerDay;
              }
              deltaCollectionData.push([data[i].dateMillis - timeOffset, growthPerDay]);
            }
 
            var c = new Array(data[i].dateMillis - timeOffset, data[i].fileCount);
            if(data[i].fileCount > fileCountDataMax) {
              fileCountDataMax = data[i].fileCount;
            }
            fileCountData.push(c);
          }
          graphDataPool[c] = {data: collectionData, dataMax: dMax, 
                              deltaData: deltaCollectionData, deltaMax: deltaDataMax,
                              fileCount: fileCountData, fileCountMax: fileCountDataMax};
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


