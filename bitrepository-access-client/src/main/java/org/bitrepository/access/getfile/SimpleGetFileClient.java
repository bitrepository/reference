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

import org.bitrepository.access.getfile.selectors.FastestPillarSelectorForGetFile;
import org.bitrepository.access.getfile.selectors.PillarSelectorForGetFile;
import org.bitrepository.access.getfile.selectors.SpecificPillarSelectorForGetFile;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.protocol.CollectionBasedConversationMediator;
import org.bitrepository.protocol.ConversationMediator;
import org.bitrepository.protocol.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default <code>GetFileClient</code>.
 */
public class SimpleGetFileClient implements GetFileClient {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    private final GetFileClientSettings settings;    
    private final MessageBus messageBus;
    private final ConversationMediator<SimpleGetFileConversation> conversationMediator;

    public SimpleGetFileClient(MessageBus messageBus, GetFileClientSettings settings) {
        conversationMediator = 
            new CollectionBasedConversationMediator<SimpleGetFileConversation>(messageBus, settings.getClientTopicID());
        this.settings = settings;
        this.messageBus = messageBus;
    }

    @Override
    public void getFileFromFastestPillar(String fileID, URL uploadUrl) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "fileID");
        ArgumentValidator.checkNotNull(uploadUrl, "uploadUrl");
        
        log.info("Requesting fastest retrieval of the file '" + fileID + "' which belong to the SLA '" + 
                settings.getBitRepositoryCollectionID() + "'.");
        startConversation(new FastestPillarSelectorForGetFile(settings.getPillarIDs()), fileID, uploadUrl);   
    }

    @Override
    public void getFileFromSpecificPillar(String fileID, URL uploadUrl, String pillarID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "fileID");
        ArgumentValidator.checkNotNull(uploadUrl, "uploadUrl");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "pillarID");

        log.info("Requesting the file '" + fileID + "' from pillar '" + pillarID + "'.");
        startConversation(new SpecificPillarSelectorForGetFile(pillarID, settings.getPillarIDs()), fileID, uploadUrl);
    }
    
    private void startConversation(PillarSelectorForGetFile selector, String fileID, URL uploadUrl) {
        SimpleGetFileConversation conversation = new SimpleGetFileConversation(
                messageBus, settings, selector, fileID, uploadUrl);
        conversationMediator.startConversation(conversation);
    }
}
