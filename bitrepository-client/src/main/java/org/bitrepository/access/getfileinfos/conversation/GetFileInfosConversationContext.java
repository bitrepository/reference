/*
 * #%L
 * BitRepository Access
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access.getfileinfos.conversation;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.OperationType;
import org.bitrepository.protocol.messagebus.MessageSender;

import java.net.URL;
import java.util.Collection;

/**
 * Encapsulates the context for a GetChecksums operation.
 */
public class GetFileInfosConversationContext extends ConversationContext {
    private final URL urlForResult;
    private final ChecksumSpecTYPE checksumSpec;
    private final ContributorQuery[] contributorQueries;

    /**
     * Extends the {@link ConversationContext} constructor with
     * {@link org.bitrepository.access.getfileinfos.GetFileInfosClient} specific parameters.
     *
     * @param collectionID          The ID of the collection
     * @param contributorQueries    See {@link org.bitrepository.access.getfileinfos.GetFileInfosClient} for details.
     * @param fileID                The ID of the file to get checksums for
     * @param checksumSpec          See {@link org.bitrepository.access.getfileinfos.GetFileInfosClient} for details.
     * @param urlForResult          See {@link org.bitrepository.access.getfileinfos.GetFileInfosClient} for details.
     * @param settings              The settings
     * @param messageSender         The MessageSender to send messages with
     * @param clientID              The ID of the client
     * @param contributors          The contributors for the conversation
     * @param eventHandler          The EventHandler to handle incoming events
     * @param auditTrailInformation The audit trail information for the contributors
     */
    public GetFileInfosConversationContext(String collectionID, ContributorQuery[] contributorQueries, String fileID,
                                           ChecksumSpecTYPE checksumSpec, URL urlForResult, Settings settings, MessageSender messageSender,
                                           String clientID, Collection<String> contributors, EventHandler eventHandler,
                                           String auditTrailInformation) {
        super(collectionID, OperationType.GET_FILE_INFOS, settings, messageSender, clientID, fileID, contributors, eventHandler,
                auditTrailInformation);
        this.contributorQueries = contributorQueries;
        this.urlForResult = urlForResult;
        this.checksumSpec = checksumSpec;
    }

    public ContributorQuery[] getContributorQueries() {
        return contributorQueries;
    }

    public URL getUrlForResult() {
        return urlForResult;
    }

    public ChecksumSpecTYPE getChecksumSpec() {
        return checksumSpec;
    }
}