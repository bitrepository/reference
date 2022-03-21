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
package org.bitrepository.access.getstatus;

import org.bitrepository.access.getstatus.conversation.GetStatusConversationContext;
import org.bitrepository.access.getstatus.conversation.IdentifyingContributorsForGetStatus;
import org.bitrepository.client.AbstractClient;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConversationBasedGetStatusClient extends AbstractClient implements GetStatusClient {
    private final Logger log = LoggerFactory.getLogger(getClass());

    public ConversationBasedGetStatusClient(
            MessageBus messageBus,
            ConversationMediator conversationMediator,
            Settings settings, String clientID) {
        super(settings, conversationMediator, messageBus, clientID);
    }

    @Override
    public void getStatus(EventHandler eventHandler) {
        ArgumentValidator.checkNotNull(eventHandler, "eventHandler");
        log.info("Requesting status for collection of components.");
        GetStatusConversationContext context = new GetStatusConversationContext(
                settings, messageBus, eventHandler, clientID,
                SettingsUtils.getStatusContributorsForCollection());
        startConversation(context, new IdentifyingContributorsForGetStatus(context));
    }
}
