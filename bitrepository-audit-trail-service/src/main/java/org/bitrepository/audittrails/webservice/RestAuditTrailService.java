/*
 * #%L
 * Bitrepository Audit Trail Service
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.audittrails.webservice;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.bitrepository.audittrails.service.AuditTrailService;
import org.bitrepository.audittrails.service.AuditTrailServiceFactory;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.ResultingStatus;

@Path("/AuditTrailService")

public class RestAuditTrailService {
    
    private AuditTrailService service;
    
    public RestAuditTrailService() {
        service = AuditTrailServiceFactory.getAuditTrailService();	
    }
      
    @GET
    @Path("/getAllAuditTrails/")
    @Produces("application/json")
    public String getAllAuditTrails() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        /*List<AuditTrailEvent> events = service.getAllAuditTrailEvents();
        Iterator<AuditTrailEvent> it = events.iterator();
        while(it.hasNext()) {
            AuditTrailEvent event = it.next();
            sb.append("{\"fileID\": \"" + event.getFileID() + "\"," +
                    "\"reportingComponent\":\" " + event.getReportingComponent() + "\"," +
                    "\"actor\":\" " + event.getActorOnFile() + "\"," +
                    "\"action\":\" " + event.getActionOnFile() + "\"," +
                    "\"timeStamp\":\" " + event.getActionDateTime() + "\"," + 
                    "\"info\":\" " + event.getInfo() + "\"," +
                    "\"auditTrailInfo\":\" " + event.getAuditTrailInformation() + "\"}");
            if(it.hasNext()) {
                sb.append(",");
            }
        }*/
        sb.append("]");
        return sb.toString();
    }

    
    @POST
    @Path("/queryAuditTrailEvents/")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public String startChecksumCheckFromPillar(
            @FormParam ("fromDate") String fromDate,
            @FormParam ("toDate") String toDate,
            @FormParam ("fileID") String fileID,
            @FormParam ("reportingComponent") String reportingComponent,
            @FormParam ("actor") String actor,
            @FormParam ("action") String action) {
        //List<AuditTrailEvent> = service.queryAuditTrailEvents(fromDate, toDate, fileID, reportingComponent, actor, action);
        return getAllAuditTrails();
    }
    
}
