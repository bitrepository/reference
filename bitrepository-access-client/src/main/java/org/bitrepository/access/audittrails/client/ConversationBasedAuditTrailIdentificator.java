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
package org.bitrepository.access.audittrails.client;

import java.util.List;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.client.AbstractClient;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;

public class ConversationBasedAuditTrailIdentificator extends AbstractClient implements AuditTrailIdentificator {
    /** The log for this class. */
    private final List<String> definedContributorIDs;
       
    public ConversationBasedAuditTrailIdentificator(Settings settings, ConversationMediator conversationMediator, MessageBus messageBus) {
        super(settings, conversationMediator, messageBus);
        this.definedContributorIDs = settings.getReferenceSettings().getAuditTrailServiceSettings().getContributors();
    }

    @Override
    public void getAvailableContributors(
            EventHandler eventHandler, String auditTrailInformation) {
        startConversation(new AuditTrailIdentificationConversation(
                settings, messageBus, eventHandler, auditTrailInformation));
    }
    
    public List<String> getDefinedContributors() {
        return definedContributorIDs;
    }
}
