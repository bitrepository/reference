<%@page pageEncoding="UTF-8"%>
<%
 HashMap<String,String>idMap = DashboardDataCache.collectionId2NameMap;
 Iterator<String> ids_iterator= idMap.keySet().iterator();

%>
<table style="font-size:10px;width:1230px;" class="table table-hover table-condensed">
 <thead>
   <tr>
    <td><b>Samlingens navn</b></td>
    <td style="text-align: right;"><b>Antal filer</b></td>
    <td style="text-align: right;"><b>Seneste Ingest</b></td>
    <td style="text-align: right;"><b>Samlingens størrelse</b></td>
    <td style="text-align: right;"><b>Ben</b></td>
    <td style="text-align: right;"><b>Seneste kontrol</b></td>
    <td style="text-align: right;"><b>Checksum fejl</b></td>
    <td style="text-align: right;"><b>Manglede filer</b></td>
    <td style="text-align: right;"><b>Næste planlagte kontrol</b></td>      
   </tr>
 </thead>
 <% while (ids_iterator.hasNext()){
    String id = ids_iterator.next();
    GetCollectionInformation collectionInfo= DashboardDataCache.getCollectionInformationMap.get(id);
    ArrayList<GetWorkflowSetup> workFlowList = DashboardDataCache.getWorkflowSetupMap.get(id);
    ArrayList<GetIntegrityStatus> integrityStatusList = DashboardDataCache.getIntegrityStatusMap.get(id); 
    int checkSumErrors = DashboardServlet.countCheckSumErrors(integrityStatusList);
    int missingFiles = DashboardServlet.countMissingFiles(integrityStatusList);
    String checkSumImage ="";
    if (checkSumErrors > 0){
      checkSumImage="info.png";
    }
    else{
      checkSumImage="check.png";
    }
    
    GetWorkflowSetup workFlow = workFlowList.get(0); //If there ever comes more than 1 I need to know which to take!
 %>
 <tr style="background-color:#ffffcc;">
   <td><%=idMap.get(id)%></td>   
   <td style="text-align: right;"><%=collectionInfo.getNumberOfFiles()%></td>
   <td style="text-align: right;"><%=collectionInfo.getLastIngest()%></td>
   <td style="text-align: right;"><%=collectionInfo.getCollectionSize()%></td>
   <td style="text-align: right;"><%=integrityStatusList.size()%></td>
   <td style="text-align: right;"><%=workFlow.getLastRun()%></td>   
   <td style="text-align: right; <%if (checkSumErrors > 0){%> color:red;<%}%>>"><%=checkSumErrors%></td>
   <td style="text-align: right; <%if (missingFiles > 0){%> color:red;<%}%>>"><%=missingFiles%></td>
   <td style="text-align: right;"><%=workFlow.getNextRun()%></td>
 </tr>
 <%}%>
 
</table>