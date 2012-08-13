/*
 * #%L
 * Bitrepository Protocol
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
package org.bitrepository.client.conversation;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.protocol.CorrelationIDGenerator;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * Encapsulates the shared state between a conversation and the related conversation states.
 */
public class ConversationContext {
    private final String conversationID;
    private final Settings settings;
    private final MessageSender messageSender;
    private final String clientID;
    private final ConversationEventMonitor monitor;
    private final String auditTrailInformation;
    private GeneralConversationState state;

    public ConversationContext(
            Settings settings,
            MessageSender messageSender,
            String clientID,
            EventHandler eventHandler,
            String auditTrailInformation) {
        this.settings = settings;
        this.messageSender = messageSender;
        this.conversationID = CorrelationIDGenerator.generateConversationID();
        this.clientID = clientID;
        this.monitor = new ConversationEventMonitor(conversationID, eventHandler);
        this.auditTrailInformation = auditTrailInformation;
    }

    public String getConversationID() {
        return conversationID;
    }

    public String getClientID() {
        return clientID;
    }

    public Settings getSettings() {
        return settings;
    }

    public MessageSender getMessageSender() {
        return messageSender;
    }

    public ConversationEventMonitor getMonitor() {
        return monitor;
    }

    public String getAuditTrailInformation() {
        return auditTrailInformation;
    }

    public GeneralConversationState getState() {
        return state;
    }
    public void setState(GeneralConversationState state) {
        this.state = state;
    }
}
