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
import java.util.List;
import org.bitrepository.access.ContributorQueryUtils;
import org.bitrepository.access.getaudittrails.client.AuditTrailConversationContext;
import org.bitrepository.access.getaudittrails.client.IdentifyingAuditTrailContributors;
import org.bitrepository.client.AbstractClient;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
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
            String collectionID,
            AuditTrailQuery[] componentQueries,
            String fileID,
            String urlForResult,
            EventHandler eventHandler, String auditTrailInformation) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "collectionID");
        validateFileID(fileID);
        if (componentQueries == null) {
            componentQueries = createFullAuditTrailQuery(collectionID);
        }
        AuditTrailConversationContext context = new AuditTrailConversationContext(
                collectionID, componentQueries, fileID, urlForResult,
                settings, messageBus, clientID, ContributorQueryUtils.getContributors(componentQueries), eventHandler,
            auditTrailInformation);
        startConversation(context, new IdentifyingAuditTrailContributors(context));
    }

    /**
     * Used to create a <code>AuditTrailQuery[]</code> array in case no array is defined.
     * @return A <code>AuditTrailQuery[]</code> array requesting all audit trails from all the defined contributers.
     */
    private AuditTrailQuery[] createFullAuditTrailQuery(String collectionId) {
        if (settings.getRepositorySettings().getGetAuditTrailSettings() == null) {
            throw new IllegalStateException("Unable getAuditTrails both undefined GetAuditTrailSettings and undefined " +
                    "AuditTrailQuery[] in getAuditTrails call");
        } else if (SettingsUtils.getAuditContributorsForCollection(collectionId).isEmpty()) {
            throw new IllegalStateException("Running AuditTrailClient without any defined contributers and undefined " +
                    "AuditTrailQuery[] in getAuditTrails call.");
        }
        Collection<String> contributers = SettingsUtils.getAuditContributorsForCollection(collectionId);
        List<AuditTrailQuery> componentQueryList = new ArrayList<AuditTrailQuery>(contributers.size());
        for (String contributer : contributers) {
            componentQueryList.add(new AuditTrailQuery(contributer, null, null, null));
        }
        return componentQueryList.toArray(new AuditTrailQuery[componentQueryList.size()]);
    }
}
