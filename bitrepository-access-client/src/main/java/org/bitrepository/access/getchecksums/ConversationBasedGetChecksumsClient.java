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
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.access.ContributorQueryUtils;
import org.bitrepository.access.getchecksums.conversation.GetChecksumsConversationContext;
import org.bitrepository.access.getchecksums.conversation.IdentifyPillarsForGetChecksums;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.AbstractClient;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The default <code>GetChecksumsClient</code>.
 * 
 * This class is just a thin wrapper which creates a conversion each time a operation is started. The conversations 
 * takes over the rest of the operation handling.
 */
public class ConversationBasedGetChecksumsClient extends AbstractClient implements GetChecksumsClient {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @see AbstractClient
     */
    public ConversationBasedGetChecksumsClient(MessageBus messageBus, ConversationMediator conversationMediator,
                                               Settings settings, String clientID) {
        super(settings, conversationMediator, messageBus, clientID);
    }


    @Override
    public void getChecksums(
            String collectionID,
            ContributorQuery[] contributorQueries,
                           String fileID,
                           ChecksumSpecTYPE checksumSpec,
                           URL addressForResult,
                           EventHandler eventHandler,
                           String auditTrailInformation) {
        validateFileID(fileID);
        if (contributorQueries == null) {
            contributorQueries = ContributorQueryUtils.createFullContributorQuery(
                settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID());
        }

        log.info("Requesting the checksums for file '" + "' with the specifications '" + checksumSpec + fileID +
            "' with query "+ Arrays.asList(contributorQueries) + "." +
            (addressForResult != null ?  "The result should be uploaded to '" + addressForResult + "'." : ""));

        GetChecksumsConversationContext context = new GetChecksumsConversationContext(collectionID,
            contributorQueries, fileID, checksumSpec, addressForResult, settings, messageBus, clientID,
            ContributorQueryUtils.getContributors(contributorQueries), eventHandler, auditTrailInformation);
        startConversation(context, new IdentifyPillarsForGetChecksums(context));
    }
}
