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

import org.bitrepository.access.getfile.conversation.GetFileConversationContext;
import org.bitrepository.access.getfile.conversation.IdentifyingPillarsForGetFile;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.client.AbstractClient;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.Collection;
import java.util.List;

/**
 * The default <code>GetFileClient</code>.
 * <p>
 * This class is just a thin wrapper which creates a conversion each time an operation is started.
 */
public class ConversationBasedGetFileClient extends AbstractClient implements GetFileClient {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @param messageBus           The MessageBus for communication
     * @param conversationMediator The ConversationMediator to keep track of conversations
     * @param settings             The settings
     * @param clientID             The ID of the client
     * @see AbstractClient
     */
    public ConversationBasedGetFileClient(MessageBus messageBus, ConversationMediator conversationMediator, Settings settings,
                                          String clientID) {
        super(settings, conversationMediator, messageBus, clientID);
        ArgumentValidator.checkNotNull(messageBus, "messageBus");
        ArgumentValidator.checkNotNull(settings, "settings");
    }

    /**
     * Attempts to get the file fom the fastest pillar (decided using the pillars' timeToDeliver ID).
     *
     * @param collectionID          Identifies the collection the file should be retrieved from.
     * @param fileID                The id of the file to retrieve.
     * @param filePart              The part of the file, which is wanted. If null, then the whole file is retrieved.
     * @param uploadUrl             The url the pillar should upload the file to.
     * @param eventHandler          The handler which should receive notifications of the progress events.
     * @param auditTrailInformation Additional information to add to the audit trail created because of this operation.
     */
    @Override
    public void getFileFromFastestPillar(String collectionID, String fileID, FilePart filePart, URL uploadUrl, EventHandler eventHandler,
                                         String auditTrailInformation) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "collectionID");
        ArgumentValidator.checkNotNull(fileID, "fileID");
        ArgumentValidator.checkNotNull(uploadUrl, "uploadUrl");
        ArgumentValidator.checkNotNull(eventHandler, "eventHandler");
        validateFileID(fileID);

        log.info("Requesting the file '{}' from the fastest pillar", fileID);
        getFile(collectionID, messageBus, settings, fileID, filePart,
                SettingsUtils.getPillarIDsForCollection(collectionID), uploadUrl, eventHandler, auditTrailInformation);
    }

    /**
     * Attempts to get the file from a specific pillar.
     *
     * @param collectionID          Identifies the collection the file should be retrieved from.
     * @param fileID                The id of the file to retrieve.
     * @param filePart              The part of the file, which is wanted. If null, then the whole file is retrieved.
     * @param uploadUrl             The url the pillar should upload the file to.
     * @param pillarID              The id of pillar, where the file should be retrieved from.
     * @param eventHandler          The handler which should receive notifications of the events occurring in connection with
     *                              the pillar communication.
     * @param auditTrailInformation Additional information to add to the audit trail created because of this operation.
     */
    @Override
    public void getFileFromSpecificPillar(String collectionID, String fileID, FilePart filePart, URL uploadUrl, String pillarID,
                                          EventHandler eventHandler, String auditTrailInformation) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "collectionID");
        ArgumentValidator.checkNotNull(fileID, "fileID");
        ArgumentValidator.checkNotNull(uploadUrl, "uploadUrl");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "pillarID");
        ArgumentValidator.checkNotNull(eventHandler, "eventHandler");
        validateFileID(fileID);

        log.info("Requesting the file '{}' from pillar '{}'", fileID, pillarID);
        getFile(collectionID, messageBus, settings, fileID, filePart, List.of(pillarID), uploadUrl, eventHandler,
                auditTrailInformation);
    }

    /**
     * Starts a conversation with GetFileConversationContext which is created using the given parameters.
     *
     * @param collectionID          The ID of the collection.
     * @param messageBus            The messageBus to use.
     * @param settings              The settings.
     * @param fileID                The ID of the file (usually the file-name).
     * @param filePart              The {@link FilePart} if only a file-part is requested.
     * @param contributors          A list of the contributors (pillars).
     * @param uploadUrl             The URL to upload to.
     * @param eventHandler          An EventHandler used to track the progress events, and await a final event.
     * @param auditTrailInformation The information given to the AuditTrail.
     */
    private void getFile(String collectionID, MessageBus messageBus, Settings settings, String fileID, FilePart filePart,
                         Collection<String> contributors, URL uploadUrl, EventHandler eventHandler, String auditTrailInformation) {
        GetFileConversationContext context = new GetFileConversationContext(collectionID, fileID, uploadUrl, filePart,
                contributors, settings, messageBus, clientID, eventHandler, auditTrailInformation);
        startConversation(context, new IdentifyingPillarsForGetFile(context));
    }
}
