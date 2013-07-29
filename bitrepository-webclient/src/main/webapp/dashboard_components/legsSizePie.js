
  var pillar_size_data;
  


  function drawPillarDataSizePieChart(url) {
    //Load data, when done, draw chart. 
    $.getJSON(url, {}, function(j) {
      pillar_size_data = new Array();
      for(i=0; i<j.length; i++) {
        pillar_size_data[i] = {label: j[i].pillarID, data: j[i].dataSize};
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
      $.plot($("#data_ben #flotcontainer_data_ben"), pillar_size_data, options);
    });
  }
