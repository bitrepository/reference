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
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

import org.bitrepository.access.getchecksums.conversation.SimpleGetChecksumsConversation;
import org.bitrepository.bitrepositoryelements.ChecksumSpecs;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.collection.settings.standardsettings.Settings;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.protocol.eventhandler.DefaultEvent;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.exceptions.NoPillarFoundException;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.exceptions.OperationTimeOutException;
import org.bitrepository.protocol.mediator.CollectionBasedConversationMediator;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default <code>GetChecksumsClient</code>.
 * 
 * This class is just a thin wrapper which creates a conversion each time a operation is started. The conversations 
 * takes over the rest of the operation handling.
 */
public class BasicGetChecksumsClient implements GetChecksumsClient {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The settings for this instance.*/
    private final Settings settings;
    /** The messagebus for communication.*/
    private final MessageBus bus;
    
    /** The mediator which should manage the conversations. */
    private final ConversationMediator<SimpleGetChecksumsConversation> conversationMediator;

    /**
     * The constructor.
     * @param messageBus The messagebus for communication.
     * @param settings The settings for this instance.
     */
    public BasicGetChecksumsClient(MessageBus messageBus, Settings settings) {
        this.bus = messageBus;
        this.settings = settings;
        conversationMediator = new CollectionBasedConversationMediator<SimpleGetChecksumsConversation>(settings, 
                bus, settings.getProtocol().getLocalDestination());
    }
    
    @Override
    public Map<String, ResultingChecksums> getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, 
            ChecksumSpecs checksumSpec, EventHandler eventHandler) {
        ArgumentValidator.checkNotNullOrEmpty(pillarIDs, "Collection<String> pillarIDs");
        ArgumentValidator.checkNotNull(fileIDs, "FileIDs fileIDs");
        ArgumentValidator.checkNotNull(checksumSpec, "ChecksumSpecTYPE checksumSpec");
        
        log.info("Requesting the checksum of the file '" + fileIDs.getFileID() + "' from the pillars '"
                + pillarIDs + "' with the specifications '" + checksumSpec + "'");
        SimpleGetChecksumsConversation conversation = new SimpleGetChecksumsConversation(bus, settings, 
                null, fileIDs, checksumSpec, pillarIDs, eventHandler);
        
        try {
            conversationMediator.addConversation(conversation);
            conversation.startConversation();
            
            while(conversation.hasEnded()) {
                try {
                    wait(500);
                } catch(InterruptedException e) {
                    log.debug("Interrupted!", e);
                }
            }
            
            return conversation.getResult();
            
        } catch (OperationFailedException e) {
            eventHandler.handleEvent(new DefaultEvent(OperationEventType.Failed, e.getMessage()));
            return null;
        }
    }

    @Override
    public Map<String, ResultingChecksums> getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, 
            ChecksumSpecs checksumSpec) throws NoPillarFoundException, OperationTimeOutException, 
            OperationFailedException {
        ArgumentValidator.checkNotNullOrEmpty(pillarIDs, "Collection<String> pillarIDs");
        ArgumentValidator.checkNotNull(fileIDs, "FileIDs fileIDs");
        ArgumentValidator.checkNotNull(checksumSpec, "ChecksumSpecTYPE checksumSpec");
        
        log.info("Requesting the checksum of the file '" + fileIDs.getFileID() + "' from the pillars '"
                + pillarIDs + "' with the specifications '" + checksumSpec + "'");
        // TODO Auto-generated method stub
        SimpleGetChecksumsConversation conversation = new SimpleGetChecksumsConversation(bus, settings, 
                null, fileIDs, checksumSpec, pillarIDs, null);
        conversationMediator.addConversation(conversation);
        conversation.startConversation();

        
        return null;
    }

    @Override
    public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecs checksumSpec, URL addressForResult,
            EventHandler eventHandler) {
        ArgumentValidator.checkNotNullOrEmpty(pillarIDs, "Collection<String> pillarIDs");
        ArgumentValidator.checkNotNull(fileIDs, "FileIDs fileIDs");
        ArgumentValidator.checkNotNull(checksumSpec, "ChecksumSpecTYPE checksumSpec");
        ArgumentValidator.checkNotNull(addressForResult, "URL addressForResult");
        
        log.info("Requesting the checksum of the file '" + fileIDs.getFileID() + "' from the pillars '"
                + pillarIDs + "' with the specifications '" + checksumSpec + "'. "
                + "The result should be uploaded to '" + addressForResult + "'.");

        SimpleGetChecksumsConversation conversation = new SimpleGetChecksumsConversation(bus, settings, 
                addressForResult, fileIDs, checksumSpec, pillarIDs, eventHandler);
        try {
            conversationMediator.addConversation(conversation);
            conversation.startConversation();
        } catch (OperationFailedException e) {
            eventHandler.handleEvent(new DefaultEvent(OperationEventType.Failed, e.getMessage()));
        }
    }

    @Override
    public void getChecksums(Collection<String> pillarIDs, FileIDs fileIDs, ChecksumSpecs checksumSpec, URL addressForResult)
            throws NoPillarFoundException, OperationTimeOutException, OperationFailedException {
        ArgumentValidator.checkNotNullOrEmpty(pillarIDs, "Collection<String> pillarIDs");
        ArgumentValidator.checkNotNull(fileIDs, "FileIDs fileIDs");
        ArgumentValidator.checkNotNull(checksumSpec, "ChecksumSpecTYPE checksumSpec");
        ArgumentValidator.checkNotNull(addressForResult, "URL addressForResult");
        
        log.info("Requesting the checksum of the file '" + fileIDs.getFileID() + "' from the pillars '"
                + pillarIDs + "' with the specifications '" + checksumSpec + "'. "
                + "The result should be uploaded to '" + addressForResult + "'.");
        SimpleGetChecksumsConversation conversation = new SimpleGetChecksumsConversation(bus, settings, 
                addressForResult, fileIDs, checksumSpec, pillarIDs, null);
        conversationMediator.addConversation(conversation);
        conversation.startConversation();
    }
}
