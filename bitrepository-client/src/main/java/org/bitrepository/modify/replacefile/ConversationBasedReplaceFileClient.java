/*
 * #%L
 * Bitrepository Modifying Client
 *
 * $Id: ConversationBasedDeleteFileClient.java 639 2011-12-15 10:24:45Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-modifying-client/src/main/java/org
 * /bitrepository/modify/deletefile/ConversationBasedDeleteFileClient.java $
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
package org.bitrepository.modify.replacefile;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.AbstractClient;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.FileSizeUtils;
import org.bitrepository.modify.replacefile.conversation.IdentifyPillarsForReplaceFile;
import org.bitrepository.modify.replacefile.conversation.ReplaceFileConversationContext;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.utils.MessageDataTypeValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;

/**
 * A conversation based implementation of the ReplaceFileClient.
 */
public class ConversationBasedReplaceFileClient extends AbstractClient implements ReplaceFileClient {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /**
     * @param messageBus           The {@link MessageBus} used for communication
     * @param conversationMediator The {@link ConversationMediator} for keeping track of conversations
     * @param settings             The {@link Settings} for the client
     * @param clientID             The ID of the client
     * @see AbstractClient
     */
    public ConversationBasedReplaceFileClient(MessageBus messageBus, ConversationMediator conversationMediator,
                                              Settings settings, String clientID) {
        super(settings, conversationMediator, messageBus, clientID);
    }

    @Override
    public void replaceFile(String collectionID, String fileID, String pillarID, ChecksumDataForFileTYPE checksumForDeleteAtPillar,
                            ChecksumSpecTYPE checksumRequestedForDeletedFile, URL url, long sizeOfNewFile,
                            ChecksumDataForFileTYPE checksumForNewFileValidationAtPillar, ChecksumSpecTYPE checksumRequestsForNewFile,
                            EventHandler eventHandler, String auditTrailInformation) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "collectionID");
        ArgumentValidator.checkNotNullOrEmpty(fileID, "fileID");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        validateFileID(fileID);
        if (settings.getRepositorySettings().getProtocolSettings().isRequireChecksumForDestructiveRequests()) {
            ArgumentValidator.checkNotNull(checksumForDeleteAtPillar,
                    "ChecksumDataForFileTYPE checksumForDeleteAtPillar");
            MessageDataTypeValidator.validate(checksumForDeleteAtPillar, "checksumForDeleteAtPillar");
        }
        if (settings.getRepositorySettings().getProtocolSettings().isRequireChecksumForNewFileRequests()) {
            ArgumentValidator.checkNotNull(checksumForNewFileValidationAtPillar,
                    "ChecksumDataForFileTYPE checksumForNewFileValidationAtPillar");
            MessageDataTypeValidator.validate(checksumForNewFileValidationAtPillar,
                    "checksumForNewFileValidationAtPillar");
        }
        MessageDataTypeValidator.validate(checksumRequestedForDeletedFile, "checksumRequestedForDeletedFile");
        MessageDataTypeValidator.validate(checksumRequestsForNewFile, "checksumRequestsForNewFile");

        log.info("Requesting replacement of file '{}' at pillar '{}' from URL '{}' with size {}," +
                        " where old file has checksum '{}' and requested checksum spec '{}'." +
                        " New file has checksum '{}' and requested checksum spec '{}'. Audit trail info: '{}'",
                fileID, pillarID, url, FileSizeUtils.toHumanShort(sizeOfNewFile), checksumForDeleteAtPillar,
                checksumRequestedForDeletedFile, checksumForNewFileValidationAtPillar, checksumRequestsForNewFile,
                auditTrailInformation);
        ReplaceFileConversationContext context = new ReplaceFileConversationContext(collectionID, fileID,
                sizeOfNewFile, url, checksumForDeleteAtPillar, checksumRequestedForDeletedFile,
                checksumForNewFileValidationAtPillar, checksumRequestsForNewFile, settings, messageBus,
                clientID, List.of(pillarID), eventHandler, auditTrailInformation);
        startConversation(context, new IdentifyPillarsForReplaceFile(context));
    }
}
