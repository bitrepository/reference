/*
 * #%L
 * Bitrepository Access
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.access.getaudittrails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.bitrepository.access.getaudittrails.client.AuditTrailConversationContext;
import org.bitrepository.access.getaudittrails.client.IdentifyingAuditTrailContributors;
import org.bitrepository.client.AbstractClient;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;

/**
 * The conversation based implementation of the {@link org.bitrepository.access.getaudittrails.AuditTrailClient}.
 */
public class ConversationBasedAuditTrailClient extends AbstractClient implements AuditTrailClient {

    public ConversationBasedAuditTrailClient(Settings settings, ConversationMediator conversationMediator, 
            MessageBus messageBus, String clientID) {
        super(settings, conversationMediator, messageBus, clientID);
    }

    @Override
    public void getAuditTrails(
            AuditTrailQuery[] componentQueries,
            String fileID,
            String urlForResult,
            EventHandler eventHandler, String auditTrailInformation) {
        validateFileID(fileID);
        if (componentQueries == null) {
            componentQueries = createFullAuditTrailQuery();
        }
        AuditTrailConversationContext context = new AuditTrailConversationContext(
                componentQueries, fileID, urlForResult,
                settings, messageBus, clientID, getContributors(componentQueries), eventHandler, auditTrailInformation);
        startConversation(context, new IdentifyingAuditTrailContributors(context));
    }

    /**
     * Used to create a <code>AuditTrailQuery[]</code> array in case no array is defined.
     * @return A <code>AuditTrailQuery[]</code> array requesting all audit trails from all the defined contributers.
     */
    private AuditTrailQuery[] createFullAuditTrailQuery() {
        if (settings.getCollectionSettings().getGetAuditTrailSettings() == null) {
            throw new IllegalStateException("Unable getAuditTrails both undefined GetAuditTrailSettings and undefined " +
                    "AuditTrailQuery[] in getAuditTrails call");
        } else if (settings.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs().isEmpty()) {
            throw new IllegalStateException("Running AuditTrailClient without any defined contributers and undefined " +
                    "AuditTrailQuery[] in getAuditTrails call.");
        }
        List<String> contributers = settings.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs();
        List<AuditTrailQuery> componentQueryList = new ArrayList<AuditTrailQuery>(contributers.size());
        for (String contributer : contributers) {
            componentQueryList.add(new AuditTrailQuery(contributer, null));
        }
        return componentQueryList.toArray(new AuditTrailQuery[componentQueryList.size()]);
    }

    /**
     * Extracts the collection of contributorIDs from the query object.
     */
    private Collection<String> getContributors(AuditTrailQuery[] queries) {
        Collection<String> contributors = new HashSet<String>();
        for (AuditTrailQuery query: queries) {
            contributors.add(query.getComponentID());
        }
        return contributors;
    }
}
