
  var collection_size_data;
  var collection_size_table_data;

  function drawCollectionDataSizePieChart(url, colorMapper) {
    //Load data, when done, draw chart. 
    $.getJSON(url, {}, function(j) {
      collection_size_data = new Array();
      collection_size_table_data = new Array();
      for(i=0; i<j.length; i++) {
        collection_size_data[i] = {label: j[i].collectionName, data: j[i].dataSize, color: colorMapper.getCollectionColor(j[i].collectionID)};
        collection_size_table_data[i] = {collection: j[i].collectionName, size: j[i].humanSize};
      }
    }).done(function() {
      var options = { series: {
                        pie: {
                          show: true,
                          radius: 1,
                          label: {
                            radius: 3/5,
                            formatter: function (label, series) { 
                              return '<div style="font-size:.85em;text-align:center;padding:5px;color:white;">' + label + '<br/>' + Math.round(series.percent) + '%</div>'; 
                            },
                            background: {
                            opacity: 0.6,
                            color: '#000'
                            }
                          }
                        }
                      },
                      legend: {show: false}
      };
      $.plot($("#collection #flotcontainer_collection"), collection_size_data, options);

      // Make legend
      var legendHtml = "<table>";
      for(i=0; i<collection_size_table_data.length; i++) {
        legendHtml += "<tr><td class=\"dataLabel\">" + collection_size_table_data[i].collection + "</td><td class=\"dataData\">" + collection_size_table_data[i].size + "</td></tr>";
      }
      legendHtml += "</table>";
      $("#collectionLegendDiv").html(legendHtml);
    });
  }

