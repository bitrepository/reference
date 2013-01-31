/*
 * #%L
 * Bitmagasin modify client
 * *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.modify.putfile;

import java.net.URL;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.AbstractClient;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.modify.putfile.conversation.IdentifyPillarsForPutFile;
import org.bitrepository.modify.putfile.conversation.PutFileConversationContext;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of the PutClient.
 */
public class ConversationBasedPutFileClient extends AbstractClient implements PutFileClient {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @see AbstractClient
     */
    public ConversationBasedPutFileClient(MessageBus messageBus, ConversationMediator conversationMediator, 
            Settings settings, String clientID) {
        super(settings, conversationMediator, messageBus, clientID);
    }
    
    @Override
    public void putFile(URL url, String fileId, long sizeOfFile, ChecksumDataForFileTYPE checksumForValidationAtPillar,
            ChecksumSpecTYPE checksumRequestsForValidation, EventHandler eventHandler, String auditTrailInformation) {
        ArgumentValidator.checkNotNull(url, "URL url");
        ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
        ArgumentValidator.checkNotNegative(sizeOfFile, "long sizeOfFile");

        log.info("Starting putFile of " + fileId + " for client " + clientID + ". " + auditTrailInformation);
        if(settings.getCollectionSettings().getProtocolSettings().isRequireChecksumForNewFileRequests()) {
            ArgumentValidator.checkNotNull(checksumForValidationAtPillar, "ChecksumDataForFileTYPE checksumForValidationAtPillar");
        }
        validateFileID(fileId);
        
        PutFileConversationContext context = new PutFileConversationContext(fileId, url, sizeOfFile, 
                checksumForValidationAtPillar, checksumRequestsForValidation, settings, messageBus,
                clientID, settings.getCollectionSettings().getClientSettings().getPillarIDs(), eventHandler, auditTrailInformation);
        startConversation(context, new IdentifyPillarsForPutFile(context));
    }

}
