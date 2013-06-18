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
    long UTC_FIX = DashboardServlet.getTimeZoneOffSetInMillis(); //Because javascript-time is local in browser. This fix makes it work in Denmark summer and winter time
    ArrayList<ArrayList<StatisticsDataSize>> allDataSizeList = (  ArrayList<ArrayList<StatisticsDataSize>> ) request.getAttribute(DashboardServlet.DATA_SIZE_HISTORY_ATTRIBUTE);
    ArrayList<String> allDataSizeIdList = (  ArrayList<String> ) request.getAttribute(DashboardServlet.DATA_SIZE_HISTORY_ID_ATTRIBUTE);

    String graphType = (String) request.getAttribute(DashboardServlet.GRAPH_TYPE_ATTRIBUTE);

    if (allDataSizeList == null){
        allDataSizeList = new ArrayList<ArrayList<StatisticsDataSize>>();
    }
    if (allDataSizeIdList == null){
        allDataSizeIdList = new ArrayList<String>();
    }

    HashMap<String,String> collectionId2NameMap = DashboardDataCache.getCollectionId2NameMap();
    HashMap<String, String> collectionId2ColorMap = DashboardDataCache.getCollectionId2ColorMap();
    long maxByteSize=DashboardServlet.getMaximumByteSize(allDataSizeList);
    String byteUnitSuffix =FileSizeUtils.toHumanUnit(maxByteSize);
    float byteUnit = FileSizeUtils.getByteSize(byteUnitSuffix);

    String y_axis_text=byteUnitSuffix.trim();
    if ("graph_tilvaekst".equals(graphType)){
        y_axis_text = byteUnitSuffix.trim();
    }
    else if ("graph_delta".equals(graphType)){
        y_axis_text = byteUnitSuffix.trim()+" pr. dag";
    }


%>

<style type="text/css">
    #placeholder .button {
        position: absolute;
        cursor: pointer;
    }
    #placeholder div.button {
        font-size: smaller;
        color: #999;
        background-color: #eee;
        padding: 2px;
    }
    #placeholder .button:hover {
        border-top-color: #28597a;
        background: #28597a;
        color: #ccc;
    }

</style>

<script>
    function changeData(){
        document.dashboardForm.submit();
    }
</script>

<script>
    function dateFormat(formattedDate) {

        var d = formattedDate.getDate();
        var m = formattedDate.getMonth()+1; //Javascript getMonths() returns 0 - 11.
        var y = formattedDate.getFullYear();
        var minutes = formattedDate.getMinutes();
        var hour = formattedDate.getHours();

        if (m <10){m = "0"+m}
        if (d <10){d = "0"+d}
        if (hour <10){hour = "0"+hour}
        if (minutes <10){minutes = "0"+minutes}

        return y + "/" + m +"/"+d +" " +hour+":"+minutes;
    }
</script>



<form name="dashboardForm" action="dashboardServlet" method="POST" class="dashboardForm">

        <select name="graphType" onchange="javascript:changeData();">
            <option <%if ("graph_tilvaekst".equals(graphType)){ out.println(" selected "); }%> value="graph_tilvaekst">Tilvækst</option>
            <option <%if ("graph_delta".equals(graphType)){ out.println(" selected "); }%> value="graph_delta">Tilvækstændring</option>
        </select>

        <%
            for ( String id : collectionId2NameMap.keySet()){
        %>
        <div class="collectionCheckBoxes"><input type="checkbox" onClick="javascript:changeData();"  <%if(request.getAttribute(id) != null){out.println("checked");}%> name="<%=id%>"> <div class="checkboxLegendWrap"><div class="checkboxLegend" style="background-color: <%=collectionId2ColorMap.get(id)%> "></div></div> <%=collectionId2NameMap.get(id)%> </div>
        <%
            }
        %>
</form>

<script>
    <%
     for (int i = 0; i< allDataSizeList.size();i++){
       ArrayList<StatisticsDataSize> dataSizeList =allDataSizeList.get(i);
     %>

    var data_tilvaekst<%=i%> = [
        <%
        for (int j = 0 ;j <dataSizeList.size();j++){
          StatisticsDataSize current = dataSizeList.get(j);%>
        [<%=(current.getDateMillis()+UTC_FIX)%> ,  <%=current.getDataSize()/byteUnit%> ],
        <%}%>
    ];

    <%}%>
    var options = {
        hoverable: true,
      
        legend:{        
            backgroundOpacity: 0.5,
            noColumns: 5,  
            position: "nw"
        }, 

        grid:  {
            hoverable: true,
            borderColor: "#cccccc"

        },
        xaxis: {  mode: "time",  localTimezone: true , zoomRange: [0.1, 10] , timeformat: "%y/%0m/%0d %0H:%0M"},
        yaxis: {  axisLabel: "<%=y_axis_text%>"},
        selection:{  mode: "xy" } ,
        points: { show: true ,  radius: 2} ,
        lines: { show: true},
        zoom: { interactive: true}
    };


    var dataObj = [
        <%
         for (int i = 0; i< allDataSizeIdList.size();i++){
         String currentId = allDataSizeIdList.get(i);
         %>

        {label:"<%=collectionId2NameMap.get(currentId)%> (<%=y_axis_text%>)", data: data_tilvaekst<%=i%> , color: '<%=collectionId2ColorMap.get(currentId)%>'},
        
        <%}%>
    ];

    $.fn.UseRange = function (plot) {
        $(this).bind("plotselected", function (event,ranges) {
            // do the zooming
            plot = $.plot($("#placeholder"), dataObj,
                    $.extend(true, {}, options, {   xaxis: { min: ranges.xaxis.from, max: ranges.xaxis.to }
                        ,   yaxis: { min: ranges.yaxis.from, max: ranges.yaxis.to }
                    }));
        });
    };

    $.fn.UseTooltip = function (plot) {
        $(this).bind("plothover", function (event, pos, item) {
            if (item) {
                if (previousPoint != item.dataIndex) {
                    previousPoint = item.dataIndex;

                    $("#tooltip").remove();

                    var x = item.datapoint[0]-<%=UTC_FIX%>;
                    var y = item.datapoint[1];

                    var d = new Date(x);
                    var formated_date = dateFormat(d);

                    showTooltip(item.pageX, item.pageY,
                            formated_date  + "<br/>  <strong>" + y +  "<%=y_axis_text%></strong><br/>" + item.series.label );

                }
            }
            else {
                $("#tooltip").remove();
                previousPoint = null;
            }

            var placeholder = $("#placeholder"); //Zoom out
            $('<div class="button" style="left:600px;top:20px">zoom out</div>').appendTo(placeholder).click(function (e) {
                e.preventDefault();

                plot.setupGrid();
                plot.draw();
                plot = $.plot("#placeholder", dataObj, options);
            });
        });
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


    $(function () {
        var plot = $.plot("#placeholder", dataObj, options);
        $("#placeholder").UseRange(plot);
        $("#placeholder").UseTooltip(plot);          
    });

</script>
<div id="placeholder"></div>
