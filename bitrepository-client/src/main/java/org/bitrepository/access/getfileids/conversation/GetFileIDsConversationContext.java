/*
 * #%L
 * Bitrepository Access
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
package org.bitrepository.access.getfileids.conversation;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.OperationType;
import org.bitrepository.protocol.messagebus.MessageSender;

import java.net.URL;
import java.util.Collection;

/**
 * Encapsulates the context for a GetFileIDs operation.
 */
public class GetFileIDsConversationContext extends ConversationContext {
    private final ContributorQuery[] contributorQueries;
    private final URL urlForResult;

    /**
     * Extends the {@link ConversationContext} constructor with
     * {@link org.bitrepository.access.getfileids.GetFileIDsClient} specific parameters.
     *
     * @param collectionID       The ID for the collection
     * @param contributorQueries See {@link org.bitrepository.access.getfileids.GetFileIDsClient} for details.
     * @param fileID             The ID of the file to get
     * @param urlForResult       See {@link org.bitrepository.access.getfileids.GetFileIDsClient} for details.
     * @param settings           The settings for the context
     * @param messageSender      MessageSender to send messages
     * @param clientID           The ID of the client
     * @param contributors       The expected contributors
     * @param eventHandler       EventHandler to handle incoming events
     */
    public GetFileIDsConversationContext(String collectionID, ContributorQuery[] contributorQueries, String fileID,
                                         URL urlForResult, Settings settings, MessageSender messageSender, String clientID,
                                         Collection<String> contributors, EventHandler eventHandler) {
        super(collectionID, OperationType.GET_FILE_IDS, settings, messageSender, clientID, fileID, contributors,
                eventHandler, null);
        this.contributorQueries = contributorQueries;
        this.urlForResult = urlForResult;
    }

    /**
     * @return The contextual {@link ContributorQuery}'s
     */
    public ContributorQuery[] getContributorQueries() {
        return contributorQueries;
    }

    /**
     * @return The URL where the results are placed (if using URL delivery)
     */
    public URL getUrlForResult() {
        return urlForResult;
    }

}
