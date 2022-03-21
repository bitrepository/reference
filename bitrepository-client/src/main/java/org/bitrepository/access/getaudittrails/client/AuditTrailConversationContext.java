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
package org.bitrepository.access.getaudittrails.client;

import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.OperationType;
import org.bitrepository.protocol.messagebus.MessageSender;

import java.util.Collection;

/**
 * models the conversation context for the {@link org.bitrepository.access.getaudittrails.AuditTrailClient}
 */
public class AuditTrailConversationContext extends ConversationContext {
    private final AuditTrailQuery[] componentQueries;
    private final String urlForResult;

    /**
     * Extends the {@link ConversationContext} constructor with {@link org.bitrepository.access.getaudittrails.AuditTrailClient} specific
     * parameters
     *
     * @param collectionID          The ID of the collection
     * @param componentQueries      The queries for the components
     * @param fileID                The fileID (usually file name)
     * @param urlForResult          The address to deliver results if delivery is requested by url
     * @param settings              The settings
     * @param messageSender         The MessageSender to send messages with
     * @param clientID              The ID of the client
     * @param contributors          The contributors in the conversation
     * @param eventHandler          The EventHandler handling incoming events
     * @param auditTrailInformation Audit trail information for the components
     */
    public AuditTrailConversationContext(
            String collectionID, AuditTrailQuery[] componentQueries, String fileID, String urlForResult,
            Settings settings, MessageSender messageSender, String clientID, Collection<String> contributors,
            EventHandler eventHandler, String auditTrailInformation) {
        super(collectionID, OperationType.GET_AUDIT_TRAILS, settings, messageSender, clientID, fileID,
                contributors, eventHandler, auditTrailInformation);
        this.componentQueries = componentQueries;
        this.urlForResult = urlForResult;
    }

    /**
     * @return The component queries
     */
    public AuditTrailQuery[] getComponentQueries() {
        return componentQueries;
    }

    /**
     * @return The URL for delivery of results
     */
    public String getUrlForResult() {
        return urlForResult;
    }
}
