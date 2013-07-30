
  var pillar_size_data;
  var pillar_size_table_data;

  function drawPillarDataSizePieChart(url) {
    //Load data, when done, draw chart. 
    $.getJSON(url, {}, function(j) {
      pillar_size_data = new Array();
      pillar_size_table_data = new Array();
      for(i=0; i<j.length; i++) {
        pillar_size_data[i] = {label: j[i].pillarID, data: j[i].dataSize};
        pillar_size_data[i] = {label: j[i].pillarID, data: j[i].humanSize};
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
      $.plot($("#data_pillar #flotcontainer_data_pillar"), pillar_size_data, options);

      // Make legend
      var legendHtml = "<table>";
      for(i=0; i<collection_size_table_data.length; i++) {
        legendHtml += "<tr><td class=\"dataLabel\">" + pillar_size_table_data[i].collection + "</td><td class=\"dataData\">" + pillar_size_table_data[i].size + "</td></tr>";
      }
      legendHtml += "</table>";
      $("#pillarLegendDiv").html(legendHtml);
    });
  }
