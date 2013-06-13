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
    ArrayList<StatisticsPillarSize> pillarSizeList = DashboardDataCache.getLatestPillarDataSize();
%>
<h3>Data fordelt på ben</h3>
<div class="legPie">
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


    <div id="data_ben">
        <div id="flotcontainer_data_ben" style="width: 280px;height:280px; text-align: left;"></div>
    </div>
</div>

<table>
    <%
        for (int i = 0 ;i < pillarSizeList .size();i++){
            StatisticsPillarSize current = pillarSizeList.get(i);%>
    <tr>
        <td class="dataLabel"><%=current.getPillarID()%>:</td> <td class="dataData"><%=FileSizeUtils.toHumanShort(current.getDataSize())%></td>
    </tr>
    <%}%>
</table>

    


