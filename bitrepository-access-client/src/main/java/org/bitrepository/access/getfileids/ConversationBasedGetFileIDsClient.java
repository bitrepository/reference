/*
 * #%L
 * Bitrepository Access
 * 
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
package org.bitrepository.access.getfileids;

import java.net.URL;
import java.util.Collection;
import org.bitrepository.access.getfileids.conversation.GetFileIDsConversationContext;
import org.bitrepository.access.getfileids.conversation.IdentifyPillarsForGetFileIDs;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.client.AbstractClient;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The reference implementation of the client side of the GetFileIDs identification and operation.
 * The default <code>GetFileIDsClient</code>
 * 
 * This class is just a thin wrapper which creates a conversion each time a operation is started. The conversations 
 * takes over the rest of the operation handling.
 */
public class ConversationBasedGetFileIDsClient extends AbstractClient implements GetFileIDsClient {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @see AbstractClient
     */
    public ConversationBasedGetFileIDsClient(MessageBus messageBus, ConversationMediator conversationMediator,
            Settings settings, String clienID) {
        super(settings, conversationMediator, messageBus, clienID);
    }

    @Override
    public void getFileIDs(Collection<String> pillarIDs, FileIDs fileIDs, URL addressForResult,
            EventHandler eventHandler, String auditTrailInformation) {
        ArgumentValidator.checkNotNullOrEmpty(pillarIDs, "Collection<String> pillarIDs");
        ArgumentValidator.checkNotNull(fileIDs, "FileIDs fileIDs");
        validateFileID(fileIDs.getFileID());
        
        log.info("Requesting the checksum of the file '" + fileIDs.getFileID() + "' from the pillars '"
                + pillarIDs + "'. The result should be uploaded to '" + addressForResult + "'.");
        
        GetFileIDsConversationContext context = new GetFileIDsConversationContext(fileIDs, addressForResult, 
                settings, messageBus, clientID, eventHandler, auditTrailInformation);
        startConversation(context, new IdentifyPillarsForGetFileIDs(context));
    }
    
}
