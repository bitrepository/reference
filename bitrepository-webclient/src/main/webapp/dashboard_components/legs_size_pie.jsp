<%@page pageEncoding="UTF-8"%>
<%
ArrayList<StatisticsPillarSize> pillarSizeList = DashboardDataCache.getLatestPillarDataSize();
%>
<center><Strong>Data fordelt på ben</Strong></center>

<script>
var data_ben = [
<%
for (int i = 0 ;i <pillarSizeList.size();i++){
  StatisticsPillarSize current = pillarSizeList.get(i);%>
  {label: "<%=current.getPillarID()%>" , data : <%=current.getDataSize()%>},
<%}%>
];
</script>

<script type="text/javascript">
$(function () { 

    var options = {
            series: {
                pie: {
                    show: true,
                    radius: 1,                    
                    label:{                        
                        radius: 3/4,
                        formatter: function (label, series) {
                            return '<div style="border:1px solid gray;font-size:8pt;text-align:center;padding:5px;color:white;">' + label + '<br/>' +   
                            Math.round(series.percent) + '%</div>';
                        },
                        background: {
                            opacity: 0.5,
                            color: '#000'
                        }
                    }
                }
                                },
            legend: {
                show: false
            }
         };

    $.plot($("#data_ben #flotcontainer_data_ben"), data_ben, options);  
});
</script>

<table cellpadding="5px" style="width: 587px">
<tr>
<td>
<div id="data_ben">    
    <div id="flotcontainer_data_ben" style="width: 300px;height:300px; text-align: left;"></div>    
</div>
</td>
<td>             
<table>
<%
for (int i = 0 ;i < pillarSizeList .size();i++){
  StatisticsPillarSize current = pillarSizeList.get(i);%>
  <tr>  
    <td style="text-align: left;"><%=current.getPillarID()%>:</td> <td style="text-align: left;"><%=FileSizeUtils.toHumanShort(current.getDataSize())%></td>  
  </tr>
<%}%>
</table>
</td>
</tr>
</table>    
    


