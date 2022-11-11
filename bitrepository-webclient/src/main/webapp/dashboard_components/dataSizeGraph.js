function DataSizeGraph(collections, colorMapper, fileSizeUtils, dataUrl, graphTypeSelector, graphPlaceholder) {
    let i;
    let collectionIDs = {};
    let colorMap = colorMapper;
    let sizeUtils = fileSizeUtils;
    let graphType = graphTypeSelector;
    let placeholder = graphPlaceholder;
    let graphDataPool = {};
    let url = dataUrl;
    let yAxisText = "y-axis text";
    let tooltipText;
    let isFloatData = true;
    let mySelf = this;
    let msPerDay = 86400 * 1000;

    for (i = 0; i < collections.length; i++) {
        collectionIDs[collections[i].collectionID] = {state : "active" };
    }

    this.enableCollection = function(collectionID) {
        collectionIDs[collectionID].state = "active";
        this.renderGraph();
    };

    this.disableCollection = function(collectionID) {
        collectionIDs[collectionID].state = "disabled";
        this.renderGraph();
    };

    this.graphTypeChanged = function() {
        this.renderGraph();
    };

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
            if (item) {
                if (previousPoint !== item.dataIndex) {
                    previousPoint = item.dataIndex;
                    $("#tooltip").remove();
                    let x = item.datapoint[0];
                    let y = item.datapoint[1];
                    let formatted_date = moment.utc(x).format("YYYY/MM/DD HH:mm");
                    let formattedValue;
                    if (isFloatData) {
                        formattedValue = numeral(y).format('0,0.0[000]');
                    } else {
                        formattedValue = numeral(y).format('0,0');
                    }
                    showTooltip(item.pageX,
                        item.pageY,
                        formatted_date  + "<br/><strong>" + formattedValue + " " + tooltipText +  "</strong>");
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
        let scaledData = [];
        for (i = 0; i < data.length; i++) {
            scaledData[i] = [data[i][0], data[i][1] / factor];
        }
        return scaledData;
    }

    this.renderGraph = function() {
        let dataObj = [];
        let dMax = 0;
        let dataField = "data";
        let dMaxField = "dataMax";
        if ($(graphType).val() === "data") {
            dataField = "data";
            dMaxField = "dataMax";
            isFloatData = true;
        } else if ($(graphType).val() === "datadelta") {
            dataField = "deltaData";
            dMaxField = "deltaMax";
            isFloatData = true;
        } else if ($(graphType).val() === "filecount") {
            dataField = "fileCount";
            dMaxField = "fileCountMax";
            isFloatData = false;
        } else if ($(graphType).val() === "filedelta") {
            dataField = "deltaCount";
            dMaxField = "deltaCountMax";
            isFloatData = true;
        }

        for (let collectionID in collectionIDs) {
            if (collectionIDs[collectionID].state === "active" && graphDataPool[collectionID] != null) {
                if (graphDataPool[collectionID][dMaxField] > dMax) {
                    dMax = graphDataPool[collectionID][dMaxField];
                }
            }
        }

        let unitSuffix = sizeUtils.toHumanUnit(dMax);
        let scale = sizeUtils.getByteSize(unitSuffix);

        for (let collectionID in collectionIDs) {
            if (collectionIDs[collectionID].state === "active" && graphDataPool[collectionID] != null) {
                let dataArray;
                //Avoid scaling y-axis data when working with filecount.
                if ($(graphType).val() === "filecount" || $(graphType).val() === "filedelta") {
                    dataArray = graphDataPool[collectionID][dataField];
                } else {
                    dataArray = scaleAndCopyData(graphDataPool[collectionID][dataField], scale);
                }

                let collectionObj = {data: dataArray, color: colorMap.getCollectionColor(collectionID)};
                dataObj.push(collectionObj);
            }
        }

        if ($(graphType).val() === "data") {
            yAxisText = unitSuffix;
            tooltipText = unitSuffix;
        } else if ($(graphType).val() === "datadelta") {
            yAxisText = unitSuffix + " per day";
            tooltipText = unitSuffix + " per day";
        } else if ($(graphType).val() === "filecount") {
            yAxisText = "Number of files";
            tooltipText = "files";
        } else if ($(graphType).val() === "filedelta") {
            yAxisText = "Files per day";
            tooltipText = "files per day";
        }

        let options = {
            hoverable: true,
            grid: {
                hoverable: true,
                borderColor: "#cccccc"
            },
            xaxis: {mode: "time",  localTimezone: true , zoomRange: [0.1, 10] , timeformat: "%y/%0m/%0d %0H:%0M"},
            yaxis: {axisLabel: yAxisText},
            selection: {mode: "xy"} ,
            points: {show: true, radius: 1},
            lines: {show: true},
            zoom: {interactive: true}
        };

        let plot = $.plot(placeholder, dataObj, options);
        useRange(placeholder, plot, dataObj, options);
        handleHover(placeholder, plot, dataObj, options);
    };

    function updateCollectionData(collection) {
        let c = collection;
        $.getJSON(url + c, {}, function(data) {
            let collectionData = [];
            let deltaCollectionData = [];
            let dMax = 0;
            let deltaDataMax = 0;
            let fileCountData = [];
            let deltaCountData = [];
            let fileCountDataMax = 0;
            let fileCountDeltaMax = 0;
            // Get the timezone offset in milliseconds
            let timeOffset = moment().tz("Europe/Copenhagen")._offset * 1000 * 60;
            for (i=0; i<data.length; i++) {
                let a = [data[i].dateMillis - timeOffset, data[i].dataSize];
                if (data[i].dataSize > dMax) {
                    dMax = data[i].dataSize;
                }
                collectionData.push(a);

                let fc = [data[i].dateMillis - timeOffset, data[i].fileCount];
                if (data[i].fileCount > fileCountDataMax) {
                    fileCountDataMax = data[i].fileCount;
                }
                fileCountData.push(fc);

                //Build delta data
                if (i === 0) {
                    deltaCollectionData.push([data[i].dateMillis - timeOffset, 0]);
                    deltaCountData.push([data[i].dateMillis - timeOffset, 0]);
                } else {
                    let deltaBytes = data[i].dataSize - data[i-1].dataSize;
                    let deltaCount = data[i].fileCount - data[i-1].fileCount;
                    let deltaMs = data[i].dateMillis - data[i-1].dateMillis;
                    let growthPerDay = (msPerDay * deltaBytes) / deltaMs;
                    if (growthPerDay > deltaDataMax) {
                        deltaDataMax = growthPerDay;
                    }
                    deltaCollectionData.push([data[i].dateMillis - timeOffset, growthPerDay]);
                    let fileGrowthPerDay = (msPerDay * deltaCount) / deltaMs;
                    if (fileGrowthPerDay > fileCountDeltaMax) {
                        fileCountDeltaMax = fileGrowthPerDay;
                    }
                    deltaCountData.push([data[i].dateMillis - timeOffset, fileGrowthPerDay]);
                }
            }
            graphDataPool[c] = {data: collectionData, dataMax: dMax,
                deltaData: deltaCollectionData, deltaMax: deltaDataMax,
                fileCount: fileCountData, fileCountMax: fileCountDataMax,
                deltaCount: deltaCountData, deltaCountMax: fileCountDeltaMax};
        }).done(function() {mySelf.renderGraph();});
    }

    this.updateData = function() {
        let keys = Object.keys(collectionIDs);
        for (i in keys) {
            updateCollectionData(keys[i]);
        }
    };

    this.getCollectionIDs = function() {
        return Object.keys(collectionIDs);
    };
}


