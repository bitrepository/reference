/*
 * #%L
 * bitrepository-access-client
 * 
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
package org.bitrepository.access.getchecksums;

import java.net.URL;
import java.util.Collection;

import javax.jms.JMSException;

import org.bitrepository.access.getchecksums.conversation.GetChecksumsConversationContext;
import org.bitrepository.access.getchecksums.conversation.SimpleGetChecksumsConversation;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.client.AbstractClient;
import org.bitrepository.client.conversation.FlowController;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.exceptions.OperationFailedException;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default <code>GetChecksumsClient</code>.
 * 
 * This class is just a thin wrapper which creates a conversion each time a operation is started. The conversations 
 * takes over the rest of the operation handling.
 */
public class CollectionBasedGetChecksumsClient extends AbstractClient implements GetChecksumsClient {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The constructor.
     * @param messageBus The messagebus for communication.
     * @param conversationMediator2 
     * @param settings The settings for this instance.
     */
    public CollectionBasedGetChecksumsClient(MessageBus messageBus, ConversationMediator conversationMediator, 
            Settings settings, String clientID) {
        super(settings, conversationMediator, messageBus, clientID);
    }

    @Override
    public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecTYPE checksumSpec, 
            URL addressForResult, EventHandler eventHandler, String auditTrailInformation)
            throws OperationFailedException {
        ArgumentValidator.checkNotNullOrEmpty(pillarIDs, "Collection<String> pillarIDs");
        ArgumentValidator.checkNotNull(fileIDs, "FileIDs fileIDs");
        ArgumentValidator.checkNotNull(checksumSpec, "ChecksumSpecTYPE checksumSpec");
        
        log.info("Requesting the checksum of the file '" + fileIDs.getFileID() + "' from the pillars '"
                + pillarIDs + "' with the specifications '" + checksumSpec + "'. "
                + "The result should be uploaded to '" + addressForResult + "'.");
        GetChecksumsConversationContext context = new GetChecksumsConversationContext(fileIDs, 
                checksumSpec, addressForResult, settings, messageBus, clientID, 
                eventHandler, auditTrailInformation);
        SimpleGetChecksumsConversation conversation = new SimpleGetChecksumsConversation(context);
        startConversation(conversation);
        
        /*SimpleGetChecksumsConversation conversation = new SimpleGetChecksumsConversation(
                bus, settings, addressForResult, fileIDs, checksumSpec, pillarIDs, clientID, eventHandler,  
                new FlowController(settings), auditTrailInformation);
        conversationMediator.addConversation(conversation);
        conversation.startConversation();*/
    }
    
}
