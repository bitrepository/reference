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
 HashMap<String,String>idMap = DashboardDataCache.getCollectionId2NameMap();
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
    GetCollectionInformation collectionInfo= DashboardDataCache.getCollectionInformationMap().get(id);
    ArrayList<GetWorkflowSetup> workFlowList = DashboardDataCache.getWorkflowSetupMap().get(id);
    ArrayList<GetIntegrityStatus> integrityStatusList = DashboardDataCache.getIntegrityStatusMap().get(id); 
    int checkSumErrors = DashboardServlet.countCheckSumErrors(integrityStatusList);
    int missingFiles = DashboardServlet.countMissingFiles(integrityStatusList);
    
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