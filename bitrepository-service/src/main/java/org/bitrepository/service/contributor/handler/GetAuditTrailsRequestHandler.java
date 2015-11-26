/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.service.contributor.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.activemq.util.ByteArrayInputStream;
import org.bitrepository.bitrepositorydata.GetAuditTrailsResults;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingAuditTrails;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.protocol.*;
import org.bitrepository.service.audit.AuditTrailDatabaseResults;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.contributor.ContributorContext;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Class for handling the GetAuditTrails operation.
 */
public class GetAuditTrailsRequestHandler extends AbstractRequestHandler<GetAuditTrailsRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The audit trail manager.*/
    private final AuditTrailManager auditManager;
    
    /**
     * Constructor.
     * @param context The context of the message handler.
     * @param auditManager the audit manager
     */
    public GetAuditTrailsRequestHandler(ContributorContext context, AuditTrailManager auditManager) {
        super(context);
        this.auditManager = auditManager;
    }

    @Override
    public Class<GetAuditTrailsRequest> getRequestClass() {
        return GetAuditTrailsRequest.class;
    }

    @Override
    public void processRequest(GetAuditTrailsRequest message, MessageContext messageContext) throws RequestHandlerException {
        validateCollectionID(message);
        validateMessage(message);
        sendProgressMessage(message);
        AuditTrailDatabaseResults resAudits = collectAudits(message);
        handleUpload(message, resAudits);
        sendFinalResponse(message, resAudits);
    }

    @Override
    public MessageResponse generateFailedResponse(GetAuditTrailsRequest request) {
        return createFinalResponse(request);
    }

    /**
     * Method for validating the content of the message.
     * @param message The message requesting the operation, which should be validated.
     * @throws InvalidMessageException if the message was invalid
     */
    protected void validateMessage(GetAuditTrailsRequest message) throws InvalidMessageException {
        if(!message.getContributor().equals(getContext().getSettings().getComponentID())) {
            throw new InvalidMessageException(
                    ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE, 
                    "Invalid contributor id.", 
                    message.getCollectionID());
        }
    }
    
    /**
     * The method for sending a progress response telling, that the operation is about to be performed.
     * @param request The request for the GetStatus operation.
     */
    protected void sendProgressMessage(GetAuditTrailsRequest request) {
        GetAuditTrailsProgressResponse response = createProgressResponse(request);
        
        // set missing variables in the request: ResponseInfo
        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Starting to extract the requested audit trails.");
        response.setResponseInfo(prInfo);

        getContext().getResponseDispatcher().dispatchResponse(response, request);
    }
    
    /**
     * Collect the requested audit trails.
     * @param message The message requesting the collecting of audit trails.
     * @return The requested audit trails.
     */
    protected AuditTrailDatabaseResults collectAudits(GetAuditTrailsRequest message) {
        Long minSeq = null;
        if(message.getMinSequenceNumber() != null) {
            log.trace("Minimum sequence value: {}", message.getMinSequenceNumber().longValue());
            minSeq = message.getMinSequenceNumber().longValue();
        }
        Long maxSeq = null;
        if(message.getMaxSequenceNumber() != null) {
            log.trace("Maximum sequence value: {}", message.getMaxSequenceNumber().longValue());
            maxSeq = message.getMaxSequenceNumber().longValue();
        }
        Date minDate = null;
        if(message.getMinTimestamp() != null) {
            log.trace("Minimum date value: {}", message.getMinTimestamp());
            minDate = CalendarUtils.convertFromXMLGregorianCalendar(message.getMinTimestamp());
        }
        Date maxDate = null;
        if(message.getMaxTimestamp() != null) {
            log.trace("Maximum date value: {}", message.getMaxTimestamp());
            maxDate = CalendarUtils.convertFromXMLGregorianCalendar(message.getMaxTimestamp());
        }
        Long maxNumberOfResults = null;
        if(message.getMaxNumberOfResults() != null) {
            log.trace("Maximum number of results: {}", message.getMaxNumberOfResults());
            maxNumberOfResults = message.getMaxNumberOfResults().longValue();
        }
        
        return auditManager.getAudits(message.getCollectionID(), message.getFileID(), minSeq, maxSeq, minDate, 
                maxDate, maxNumberOfResults);
    }
    
    /**
     * Handles the potential upload of the audit trails to a given URL.
     * @param message The request for the audit trails, which includes the URL for where the audit trails should be 
     * uploaded
     * @param extractedAuditTrails The extracted audit trails.
     * @throws InvalidMessageException If the creation, serialization, validation or upload of the file fails.
     */
    protected void handleUpload(GetAuditTrailsRequest message, AuditTrailDatabaseResults extractedAuditTrails) throws InvalidMessageException {
        if(message.getResultAddress() == null || message.getResultAddress().isEmpty()) {
            log.trace("The audit trails are not uploaded.");
            return;
        }
        
        log.debug("Creating audit trail file and uploading it.");
        try {
            File fileToUpload = createAuditTrailFile(message, extractedAuditTrails);
            URL uploadUrl = new URL(message.getResultAddress());
            
            log.debug("Uploading file: " + fileToUpload.getName() + " to " + uploadUrl.toExternalForm());
            getContext().getFileExchange().putFile(new FileInputStream(fileToUpload), uploadUrl);
        } catch (Exception e) {
            throw new InvalidMessageException(
                    ResponseCode.FILE_TRANSFER_FAILURE, 
                    "Could not handle the creation and upload of the results due to: " + e.getMessage(), 
                    message.getCollectionID(), e);
        }
    }
    
    /**
     * Creates a file containing all the audit trails.
     * @param request The request for the data.
     * @param extractedAuditTrails The extracted audit trails.
     * @return The file containing the extracted audit trails.
     * @throws IOException If something goes wrong when creating the file or finding the XSD.
     * @throws JAXBException If the resulting structure cannot be serialized.
     * @throws SAXException If the results does not validate against the XSD.
     */
    protected File createAuditTrailFile(GetAuditTrailsRequest request, AuditTrailDatabaseResults extractedAuditTrails) 
            throws IOException, JAXBException, SAXException {
        File checksumResultFile = File.createTempFile(request.getCorrelationID(), new Date().getTime() + ".at");
        
        GetAuditTrailsResults results = new GetAuditTrailsResults();
        results.setVersion(ProtocolVersionLoader.loadProtocolVersion().getVersion());
        results.setMinVersion(ProtocolVersionLoader.loadProtocolVersion().getMinVersion());
        results.setCollectionID(request.getCollectionID());
        results.getAuditTrailEvents().add(extractedAuditTrails.getAuditTrailEvents());

        OutputStream is = null;
        try {
            is = new FileOutputStream(checksumResultFile);
            JaxbHelper jaxb = new JaxbHelper(XSD_CLASSPATH, XSD_BR_DATA);
            String xmlMessage = jaxb.serializeToXml(results);
            jaxb.validate(new ByteArrayInputStream(xmlMessage.getBytes()));
            is.write(xmlMessage.getBytes());
            is.flush();
        } finally {
            if(is != null) {
                is.close();
            }
        }
        
        return checksumResultFile;
    }

    /**
     * Method for sending a positive final response.
     * @param request The request to respond to.
     * @param auditExtract The retrieved audit trails.
     */
    protected void sendFinalResponse(GetAuditTrailsRequest request, AuditTrailDatabaseResults auditExtract) {
        GetAuditTrailsFinalResponse response = createFinalResponse(request);
        ResultingAuditTrails resAuditTrails = new ResultingAuditTrails();
        if(request.getResultAddress() == null) {
            resAuditTrails.setAuditTrailEvents(auditExtract.getAuditTrailEvents());
        } else {
            resAuditTrails.setResultAddress(request.getResultAddress());
        }
        
        response.setResultingAuditTrails(resAuditTrails);
        response.setPartialResult(auditExtract.moreResults());
        
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        response.setResponseInfo(responseInfo);

        getContext().getResponseDispatcher().dispatchResponse(response, request);
    }
    
    /**
     * Creates a GetStatusProgressResponse based on a GetStatusRequest. Missing the 
     * following fields (besides the ones in dispatchResponse):
     * <br/> - ResponseInfo
     * 
     * @param message The message to base the response upon.
     * @return The GetStatusProgressResponse based on the request.
     */
    private GetAuditTrailsProgressResponse createProgressResponse(GetAuditTrailsRequest message) {
        GetAuditTrailsProgressResponse res = new GetAuditTrailsProgressResponse();
        res.setContributor(getContext().getSettings().getComponentID());
        res.setResultAddress(message.getResultAddress());
        
        return res;
    }
    
    /**
     * Creates a GetStatusFinalResponse based on a GetStatusRequest. Missing the 
     * following fields (besides the ones in dispatchResponse):
     * <ul>
     * <li>ResponseInfo</li>
     * <li>ResultingAuditTrails</li>
     * </ul>
     * @param request the audit trail request. Ignored
     * @return The GetStatusFinalResponse based on the request.
     */
    protected GetAuditTrailsFinalResponse createFinalResponse(GetAuditTrailsRequest request) {
        GetAuditTrailsFinalResponse res = new GetAuditTrailsFinalResponse();
        res.setContributor(getContext().getSettings().getComponentID());
        
        return res;
    }
}
