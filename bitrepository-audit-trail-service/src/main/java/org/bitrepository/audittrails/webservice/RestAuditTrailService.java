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

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.bitrepository.audittrails.AuditTrailService;
import org.bitrepository.audittrails.AuditTrailServiceFactory;
import org.bitrepository.audittrails.store.AuditEventIterator;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonGenerator;

@Path("/AuditTrailService")

public class RestAuditTrailService {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    private AuditTrailService service;
    
    public RestAuditTrailService() {
        service = AuditTrailServiceFactory.getAuditTrailService();	
    }
        
    @POST
    @Path("/queryAuditTrailEvents/")
    @Consumes("application/x-www-form-urlencoded")
    @Produces("application/json")
    public StreamingOutput queryAuditTrailEvents(
            @FormParam("fromDate") String fromDate,
            @FormParam("toDate") String toDate,
            @FormParam("fileID") String fileID,
            @FormParam("reportingComponent") String reportingComponent,
            @FormParam("actor") String actor,
            @FormParam("action") String action,
            @FormParam("collectionID") String collectionID,
            @FormParam("fingerprint") String fingerprint,
            @FormParam("operationID") String operationID,
            @DefaultValue("1000") @FormParam("maxAudittrails") Integer maxResults) {
        Date from = CalendarUtils.makeStartDateObject(fromDate);
        Date to = CalendarUtils.makeEndDateObject(toDate);
        
        final int maxAudits = maxResults;
        final AuditEventIterator it = service.queryAuditTrailEventsByIterator(from, to, contentOrNull(fileID),
                collectionID, contentOrNull(reportingComponent), contentOrNull(actor), filterAction(action), 
                contentOrNull(fingerprint), contentOrNull(operationID));
        if(it != null) {     
            return new StreamingOutput() {
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    JsonFactory jf = new JsonFactory();
                    JsonGenerator jg = jf.createGenerator(output, JsonEncoding.UTF8);
                    try {
                        AuditTrailEvent event;
                        jg.writeStartArray();
                        int numAudits = 0;
                        while((event = it.getNextAuditTrailEvent()) != null && numAudits < maxAudits) {
                            writeAuditResult(event, jg);
                            numAudits++;
                        }
                        jg.writeEndArray();
                        jg.flush();
                        jg.close();
                    } catch (Exception e) {
                        log.error("Caught exception trying to stream audittrails", e);
                        throw new WebApplicationException(e);
                    } finally {
                        try {
                            if(it != null) {
                                it.close();
                            }
                        } catch (Exception e) {
                            log.error("Caught exception when closing AuditEventIterator", e);
                            throw new WebApplicationException(e);
                        }
                    }
                }
            };
        } else {
            throw new WebApplicationException(Response.status(Response.Status.NO_CONTENT)
                    .entity("Failed to get audit trails from database")
                    .type(MediaType.TEXT_PLAIN)
                    .build());
        }
    }

    @POST
    @Path("/collectAuditTrails/")
    @Produces("text/html")
    public String collectAuditTrails() {
        service.collectAuditTrails();
        return "Started audittrails collection";
    }
    
    @GET
    @Path("/collectionSchedule")
    @Produces(MediaType.APPLICATION_JSON)
    public List<CollectorInfo> getCollectionSchedule() {
        return service.getCollectorInfos();
    }
    
    private void writeAuditResult(AuditTrailEvent event, JsonGenerator jg) throws JsonGenerationException, IOException {
        jg.writeStartObject();
        jg.writeObjectField("fileID", event.getFileID());
        jg.writeObjectField("reportingComponent", event.getReportingComponent());
        jg.writeObjectField("actor", contentOrEmptyString(event.getActorOnFile()));
        jg.writeObjectField("action", event.getActionOnFile().toString());
        jg.writeObjectField("timeStamp", TimeUtils.shortDate(
                CalendarUtils.convertFromXMLGregorianCalendar(event.getActionDateTime())));
        jg.writeObjectField("info", contentOrEmptyString(event.getInfo()));
        jg.writeObjectField("auditTrailInfo", contentOrEmptyString(event.getAuditTrailInformation()));
        jg.writeObjectField("fingerprint", contentOrEmptyString(event.getCertificateID()));
        jg.writeObjectField("operationID", contentOrEmptyString(event.getOperationID()));
        jg.writeEndObject();
        jg.flush();
        
    }

    private FileAction filterAction(String action) {
        if(action.equals("ALL")) {
            return null;
        } else {
            return FileAction.fromValue(action);
        }
    }

    private String contentOrEmptyString(String input) {
        if(input == null) {
            return "";
        } else {
            return input.trim();
        }
    }
    
    private String contentOrNull(String input) {
        if(input != null && input.trim().isEmpty()) {
            return null;
        } else {
            return input.trim();
        }
    }
}
