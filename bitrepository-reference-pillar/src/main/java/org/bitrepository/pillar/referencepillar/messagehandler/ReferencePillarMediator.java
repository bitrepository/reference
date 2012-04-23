/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar.referencepillar.messagehandler;

import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.pillar.common.GetAuditTrailsRequestHandler;
import org.bitrepository.pillar.common.GetStatusRequestHandler;
import org.bitrepository.pillar.common.IdentifyContributorsForGetAuditTrailsRequestHandler;
import org.bitrepository.pillar.common.IdentifyContributorsForGetStatusRequestHandler;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.common.PillarMediator;
import org.bitrepository.pillar.referencepillar.ReferenceArchive;

/**
 * This instance handles the conversations for the reference pillar.
 * It only responds to requests. It does not it self start conversations, though it might send Alarms when something 
 * is not right.
 * All other messages than requests are considered garbage.
 * Every message (even garbage) is put into the audit trails.
 */
public class ReferencePillarMediator extends PillarMediator {
    /**
     * The archive for this pillar mediator.
     */
    private final ReferenceArchive archive;
    
    /**
     * Constructor.
     * Sets the parameters of this mediator, and adds itself as a listener to the destinations.
     * 
     * @param messagebus The messagebus for this instance.
     * @param settings The settings for the reference pillar.
     * @param refArchive The archive for the reference pillar.
     */
    public ReferencePillarMediator(PillarContext context, ReferenceArchive archive) {
        super(context);
        this.archive = archive;
        
        // Initialise the messagehandlers.
        initialiseHandlers(context);
    }
    
    @Override
    protected void initialiseHandlers(PillarContext context) {
        this.handlers.put(IdentifyPillarsForGetFileRequest.class.getName(), 
                new IdentifyPillarsForGetFileRequestHandler(context, archive));
        this.handlers.put(GetFileRequest.class.getName(), 
                new GetFileRequestHandler(context, archive));
        this.handlers.put(IdentifyPillarsForGetFileIDsRequest.class.getName(), 
                new IdentifyPillarsForGetFileIDsRequestHandler(context, archive));
        this.handlers.put(GetFileIDsRequest.class.getName(), 
                new GetFileIDsRequestHandler(context, archive));
        this.handlers.put(IdentifyPillarsForGetChecksumsRequest.class.getName(), 
                new IdentifyPillarsForGetChecksumsRequestHandler(context, archive));
        this.handlers.put(GetChecksumsRequest.class.getName(), 
                new GetChecksumsRequestHandler(context, archive));
        
        this.handlers.put(IdentifyContributorsForGetStatusRequest.class.getName(), 
                new IdentifyContributorsForGetStatusRequestHandler(context));
        this.handlers.put(GetStatusRequest.class.getName(),
                new GetStatusRequestHandler(context));
        this.handlers.put(IdentifyContributorsForGetAuditTrailsRequest.class.getName(), 
                new IdentifyContributorsForGetAuditTrailsRequestHandler(context));
        this.handlers.put(GetAuditTrailsRequest.class.getName(), 
                new GetAuditTrailsRequestHandler(context));
        
        this.handlers.put(IdentifyPillarsForPutFileRequest.class.getName(), 
                new IdentifyPillarsForPutFileRequestHandler(context, archive));
        this.handlers.put(PutFileRequest.class.getName(), 
                new PutFileRequestHandler(context, archive));
        this.handlers.put(IdentifyPillarsForDeleteFileRequest.class.getName(), 
                new IdentifyPillarsForDeleteFileRequestHandler(context, archive));
        this.handlers.put(DeleteFileRequest.class.getName(), 
                new DeleteFileRequestHandler(context, archive));
        this.handlers.put(IdentifyPillarsForReplaceFileRequest.class.getName(), 
                new IdentifyPillarsForReplaceFileRequestHandler(context, archive));
        this.handlers.put(ReplaceFileRequest.class.getName(), 
                new ReplaceFileRequestHandler(context, archive));
    }    
}
