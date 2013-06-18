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
<h3> Data fordelt på samlinger</h3>
<div class="collenctionPie">
    <script>
        var data_samling = [
            <%
            for (int i = 0 ;i <collectionSizeList.size();i++){
              StatisticsCollectionSize current = collectionSizeList.get(i);
              String currentId = current.getCollectionID();
            %>
            {label: "<%=DashboardDataCache.getCollectionId2NameMap().get(currentId)%>" , data : <%=current.getDataSize()%> , color : '<%=DashboardDataCache.getCollectionId2ColorMap().get(currentId)%>' },
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
                            radius: 3/5,
                            formatter: function (label, series) {
                                return '<div style="font-size:.85em;text-align:center;padding:5px;color:white;">' + label + '<br/>' +
                                        Math.round(series.percent) + '%</div>';
                            },
                            background: {
                                opacity: 0.6,
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


    <div id="samling">
        <div id="flotcontainer_samling" style="width: 280px;height:280px; text-align: left;"></div>
    </div>
</div>

<table>
    <%
        for (int i = 0 ;i < collectionSizeList.size();i++){
            StatisticsCollectionSize current = collectionSizeList.get(i);%>
    <tr>
        <td class="dataLabel"><%=DashboardDataCache.getCollectionId2NameMap().get(current.getCollectionID())%>:</td> <td class="dataData"><%=FileSizeUtils.toHumanShort(current.getDataSize())%></td>
    </tr>
    <%}%>
</table>
