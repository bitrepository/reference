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
package org.bitrepository.pillar.checksumpillar.messagehandler;

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.pillar.cache.ChecksumStore;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.common.PillarMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.service.contributor.handler.GetAuditTrailsRequestHandler;
import org.bitrepository.service.contributor.handler.GetStatusRequestHandler;
import org.bitrepository.service.contributor.handler.IdentifyContributorsForGetAuditTrailsRequestHandler;
import org.bitrepository.service.contributor.handler.IdentifyContributorsForGetStatusRequestHandler;
import org.bitrepository.service.contributor.handler.RequestHandler;

/**
 * This instance handles the conversations for the checksum pillar.
 * It only responds to requests. It does not it self start conversations, though it might send Alarms when something 
 * is not right.
 * All other messages than requests are considered garbage.
 * Every message (even garbage) is currently put into the audit trails.
 */
public class ChecksumPillarMediator extends PillarMediator {
    private final ChecksumStore cache;
    
    /**
     * Constructor.
     * Sets the parameters of this mediator, and adds itself as a listener to the destinations.
     */
    public ChecksumPillarMediator(MessageBus messageBus, MessageHandlerContext context, ChecksumStore refCache) {
        super(messageBus, context);
        ArgumentValidator.checkNotNull(refCache, "ChecksumCache refCache");
        this.cache = refCache;
    }

    @Override
    protected RequestHandler[] createListOfHandlers() {
        List<RequestHandler> res = new ArrayList<RequestHandler>();
        res.add(new IdentifyPillarsForGetFileRequestHandler(getPillarContext(), cache));
        res.add(new GetFileRequestHandler(getPillarContext(), cache));
        res.add(new IdentifyPillarsForGetFileIDsRequestHandler(getPillarContext(), cache));
        res.add(new GetFileIDsRequestHandler(getPillarContext(), cache));
        res.add(new IdentifyPillarsForGetChecksumsRequestHandler(getPillarContext(), cache));
        res.add(new GetChecksumsRequestHandler(getPillarContext(), cache));
        
        res.add(new IdentifyContributorsForGetStatusRequestHandler(getContext()));
        res.add(new GetStatusRequestHandler(getContext()));
        res.add(new IdentifyContributorsForGetAuditTrailsRequestHandler(getContext()));
        res.add(new GetAuditTrailsRequestHandler(getContext(), getPillarContext().getAuditTrailManager()));
        
        res.add(new IdentifyPillarsForPutFileRequestHandler(getPillarContext(), cache));
        res.add(new PutFileRequestHandler(getPillarContext(), cache));
        res.add(new IdentifyPillarsForDeleteFileRequestHandler(getPillarContext(), cache));
        res.add(new DeleteFileRequestHandler(getPillarContext(), cache));
        res.add(new IdentifyPillarsForReplaceFileRequestHandler(getPillarContext(), cache));
        res.add(new ReplaceFileRequestHandler(getPillarContext(), cache));
        
        return res.toArray(new RequestHandler[res.size()]);
    }
}
