/*
 * #%L
 * Bitrepository Access Client
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
package org.bitrepository.access.getfile;

import java.net.URL;
import java.util.Arrays;
import java.util.Collection;
import org.bitrepository.access.getfile.conversation.GetFileConversationContext;
import org.bitrepository.access.getfile.conversation.IdentifyingPillarsForGetFile;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.client.AbstractClient;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default <code>GetFileClient</code>.
 * 
 * This class is just a thin wrapper which creates a conversion each time a operation is started. The conversations 
 * takes over the rest of the operation handling.
 */
public class ConversationBasedGetFileClient extends AbstractClient implements GetFileClient {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @see AbstractClient
     */
    public ConversationBasedGetFileClient(MessageBus messageBus, ConversationMediator conversationMediator, Settings settings,
                                          String clientID) {
        super(settings, conversationMediator, messageBus, clientID);
        ArgumentValidator.checkNotNull(messageBus, "messageBus");
        ArgumentValidator.checkNotNull(settings, "settings");
    }

    @Override
    public void getFileFromFastestPillar(String fileID, FilePart filePart, URL uploadUrl, EventHandler eventHandler) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "fileID");
        ArgumentValidator.checkNotNull(uploadUrl, "uploadUrl");
        ArgumentValidator.checkNotNull(eventHandler, "eventHandler");
        validateFileID(fileID);
        
        log.info("Requesting the file '" + fileID + " from the fastest pillar");
        getFile(messageBus, settings, fileID, filePart,
                settings.getCollectionSettings().getClientSettings().getPillarIDs(),
                 uploadUrl, eventHandler);
    }

    @Override
    public void getFileFromSpecificPillar(String fileID, FilePart filePart, URL uploadUrl, String pillarID, EventHandler eventHandler) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "fileID");
        ArgumentValidator.checkNotNull(uploadUrl, "uploadUrl");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "pillarID");
        ArgumentValidator.checkNotNull(eventHandler, "eventHandler");
        validateFileID(fileID);
        
        log.info("Requesting the file '" + fileID + "' from pillar '" + pillarID + "'.");
        getFile(messageBus, settings, fileID, filePart, Arrays.asList(pillarID),
                uploadUrl, eventHandler);
    }

    private void getFile(MessageBus messageBus, Settings settings,
            String fileID, FilePart filePart, Collection<String> contributors, URL uploadUrl, EventHandler eventHandler) {
        GetFileConversationContext context = new GetFileConversationContext(
                fileID, uploadUrl, filePart, contributors, settings, messageBus, clientID, eventHandler, null
        );
        startConversation(context, new IdentifyingPillarsForGetFile(context));
    }
}
