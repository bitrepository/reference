
  var collection_size_data

  function drawCollectionDataSizePieChart(url) {
    //Load data, when done, draw chart. 
    $.getJSON(url, {}, function(j) {
      collection_size_data = new Array();
      for(i=0; i<j.length; i++) {
        collection_size_data[i] = {label: j[i].collectionID, data: j[i].dataSize};
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
    });
  }

