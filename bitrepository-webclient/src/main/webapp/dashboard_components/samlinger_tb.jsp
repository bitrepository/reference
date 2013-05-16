<%@page pageEncoding="UTF-8"%>
<%
ArrayList<StatisticsCollectionSize> collectionSizeList = DashboardDataCache.getCollectionDataSize();
%>
<center><Strong>Samlinger fordelt på TB</Strong></center>

<script>
var data_samling = [
<%
for (int i = 0 ;i <collectionSizeList.size();i++){
  StatisticsCollectionSize current = collectionSizeList.get(i);%>

  {label: "<%=DashboardDataCache.collectionId2NameMap.get(current.getCollectionID())%>" , data : <%=current.getDataSize()%>},
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

    $.plot($("#samling #flotcontainer_samling"), data_samling, options);  
});
</script>

<table cellpadding="5px" style="width: 587px">
<tr>
<td>
<div id="samling">
    <div id="flotcontainer_samling" style="width: 300px;height:300px; text-align: left;"></div>    
</div>
</td>
<td>             
<table>
<%
for (int i = 0 ;i < collectionSizeList.size();i++){
  StatisticsCollectionSize current = collectionSizeList.get(i);%>
  <tr>
    <td style="text-align: left;"><%=DashboardDataCache.collectionId2NameMap.get(current.getCollectionID())%>:</td> <td style="text-align: left;"><%=FileSizeUtils.toHumanShort(current.getDataSize())%></td>
  </tr>  
<%}%>
</table>
</td>
</tr>
</table>    

