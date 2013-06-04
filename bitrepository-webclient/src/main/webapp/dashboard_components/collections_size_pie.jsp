<%--
  #%L
  Bitrepository Webclient
  %%
  Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
  %%
  This program is free software: you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as 
  published by the Free Software Foundation, either version 2.1 of the 
  License, or (at your option) any later version.
  
  This program is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Lesser Public License for more details.
  
  You should have received a copy of the GNU General Lesser Public 
  License along with this program.  If not, see
  <http://www.gnu.org/licenses/lgpl-2.1.html>.
  #L%
  --%>
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
  {label: "<%=DashboardDataCache.getCollectionId2NameMap().get(current.getCollectionID())%>" , data : <%=current.getDataSize()%>},
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
    <td style="text-align: left;"><%=DashboardDataCache.getCollectionId2NameMap().get(current.getCollectionID())%>:</td> <td style="text-align: left;"><%=FileSizeUtils.toHumanShort(current.getDataSize())%></td>
  </tr>  
<%}%>
</table>
</td>
</tr>
</table>