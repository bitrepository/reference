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

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.common.PillarMediator;
import org.bitrepository.pillar.referencepillar.archive.ReferenceArchive;
import org.bitrepository.service.contributor.handler.GetAuditTrailsRequestHandler;
import org.bitrepository.service.contributor.handler.GetStatusRequestHandler;
import org.bitrepository.service.contributor.handler.IdentifyContributorsForGetAuditTrailsRequestHandler;
import org.bitrepository.service.contributor.handler.IdentifyContributorsForGetStatusRequestHandler;
import org.bitrepository.service.contributor.handler.RequestHandler;

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
    }

    @SuppressWarnings("rawtypes")
    @Override
    protected RequestHandler[] createListOfHandlers() {
        List<RequestHandler> handlers = new ArrayList<RequestHandler>();
        
        handlers.add(new IdentifyPillarsForGetFileRequestHandler(getPillarContext(), archive));
        handlers.add(new GetFileRequestHandler(getPillarContext(), archive));
        handlers.add(new IdentifyPillarsForGetFileIDsRequestHandler(getPillarContext(), archive));
        handlers.add(new GetFileIDsRequestHandler(getPillarContext(), archive));
        handlers.add(new IdentifyPillarsForGetChecksumsRequestHandler(getPillarContext(), archive));
        handlers.add(new GetChecksumsRequestHandler(getPillarContext(), archive));
        
        handlers.add(new IdentifyContributorsForGetStatusRequestHandler(getContext()));
        handlers.add(new GetStatusRequestHandler(getContext()));
        handlers.add(new IdentifyContributorsForGetAuditTrailsRequestHandler(getContext()));
        handlers.add(new GetAuditTrailsRequestHandler(getContext(), getPillarContext().getAuditTrailManager()));
        
        handlers.add(new IdentifyPillarsForPutFileRequestHandler(getPillarContext(), archive));
        handlers.add(new PutFileRequestHandler(getPillarContext(), archive));
        handlers.add(new IdentifyPillarsForDeleteFileRequestHandler(getPillarContext(), archive));
        handlers.add(new DeleteFileRequestHandler(getPillarContext(), archive));
        handlers.add(new IdentifyPillarsForReplaceFileRequestHandler(getPillarContext(), archive));
        handlers.add(new ReplaceFileRequestHandler(getPillarContext(), archive));
        
        return handlers.toArray(new RequestHandler[handlers.size()]);
    }    
}
