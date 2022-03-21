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

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.AbstractClient;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.modify.putfile.conversation.IdentifyPillarsForPutFile;
import org.bitrepository.modify.putfile.conversation.PutFileConversationContext;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.utils.MessageDataTypeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;

public class ConversationBasedPutFileClient extends AbstractClient implements PutFileClient {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @param messageBus           The {@link MessageBus} for handling communication
     * @param conversationMediator The {@link ConversationMediator} for keeping track of conversations
     * @param settings             The {@link Settings} for the client
     * @param clientID             The ID of the client
     * @see AbstractClient
     */
    public ConversationBasedPutFileClient(MessageBus messageBus, ConversationMediator conversationMediator, Settings settings,
                                          String clientID) {
        super(settings, conversationMediator, messageBus, clientID);
    }

    @Override
    public void putFile(String collectionID, URL url, String fileID, long sizeOfFile, ChecksumDataForFileTYPE checksumForValidationAtPillar,
                        ChecksumSpecTYPE checksumRequestsForValidation, EventHandler eventHandler, String auditTrailInformation) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "collectionID");
        ArgumentValidator.checkNotNullOrEmpty(fileID, "fileID");
        validateFileID(fileID);
        ArgumentValidator.checkNotNull(url, "URL url");
        ArgumentValidator.checkNotNegative(sizeOfFile, "long sizeOfFile");
        MessageDataTypeValidator.validate(checksumForValidationAtPillar, "checksumForValidationAtPillar");
        MessageDataTypeValidator.validate(checksumRequestsForValidation, "checksumRequestsForValidation");

        log.info("Starting putFile of " + fileID + " for client " + clientID + ". " + auditTrailInformation);
        if (settings.getRepositorySettings().getProtocolSettings().isRequireChecksumForNewFileRequests()) {
            ArgumentValidator.checkNotNull(checksumForValidationAtPillar, "ChecksumDataForFileTYPE checksumForValidationAtPillar");
        }

        PutFileConversationContext context = new PutFileConversationContext(collectionID, fileID, url, sizeOfFile,
                checksumForValidationAtPillar, checksumRequestsForValidation, settings, messageBus, clientID,
                SettingsUtils.getPillarIDsForCollection(collectionID), eventHandler, auditTrailInformation);
        startConversation(context, new IdentifyPillarsForPutFile(context));
    }
}
