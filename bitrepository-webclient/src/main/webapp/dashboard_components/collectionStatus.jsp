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

<table class="table table-hover table-condensed">
    <thead>
    <tr>
        <th class="collectionName">Samlingens navn</th>
        <th>Antal filer</th>
        <th>Seneste Ingest</th>
        <th>Samlingens størrelse</th>
        <th>Ben</th>
        <th>Seneste kontrol</th>
        <th>Checksum fejl</th>
        <th>Manglende filer</th>
        <th>Næste planlagte kontrol</th>
    </tr>
    </thead>
<!--    <% while (ids_iterator.hasNext()){
        String id = ids_iterator.next();
        GetCollectionInformation collectionInfo= DashboardDataCache.getCollectionInformationMap().get(id);
        ArrayList<GetWorkflowSetup> workFlowList = DashboardDataCache.getWorkflowSetupMap().get(id);
        ArrayList<GetIntegrityStatus> integrityStatusList = DashboardDataCache.getIntegrityStatusMap().get(id);
        int checkSumErrors = DashboardServlet.countCheckSumErrors(integrityStatusList);
        int missingFiles = DashboardServlet.countMissingFiles(integrityStatusList);

        GetWorkflowSetup workFlow = workFlowList.get(0); //If there ever comes more than 1 I need to know which to take!
    %>
    <tr>
        <td class="collectionName"><%=idMap.get(id)%></td>
        <td><%=collectionInfo.getNumberOfFiles()%></td>
        <td><%=collectionInfo.getLastIngest()%></td>
        <td><%=collectionInfo.getCollectionSize()%></td>
        <td><%=integrityStatusList.size()%></td>
        <td><%=workFlow.getLastRun()%></td>
        <td class="<%if (checkSumErrors > 0){%>error<%}%>"><%=checkSumErrors%></td>
        <td class="<%if (missingFiles > 0){%>error<%}%>"><%=missingFiles%></td>
        <td><%=workFlow.getNextRun()%></td>
    </tr>
    <%}%>
-->
</table>
