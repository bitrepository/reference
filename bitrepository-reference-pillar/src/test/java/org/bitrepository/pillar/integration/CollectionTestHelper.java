package org.bitrepository.pillar.integration;
/*
 * #%L
 * Bitrepository Reference Pillar
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

import java.util.Collection;

import org.bitrepository.access.getfileids.ConversationBasedGetFileIDsClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.modify.deletefile.ConversationBasedDeleteFileClient;
import org.bitrepository.modify.deletefile.DeleteFileClient;
import org.bitrepository.modify.putfile.ConversationBasedPutFileClient;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.fileexchange.HttpServerConfiguration;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;

@SuppressWarnings("unused")
public class CollectionTestHelper {
    private final Settings settings;
    private final HttpServerConfiguration httpServerConfiguration;
    private final SecurityManager securityManager;

    private final GetFileIDsClient getFileIDsClient;
    private final DeleteFileClient deleteFileClient;
    private final ConversationMediator conversationMediator;
    private final MessageBus messageBus;
    protected PutFileClient putClient;

    public CollectionTestHelper(
            Settings settings,
            HttpServerConfiguration httpServerConfiguration) {
        this.settings = settings;
        this.securityManager = new DummySecurityManager();
        this.httpServerConfiguration = httpServerConfiguration;

        SecurityManager securityManager1 = new DummySecurityManager();
        messageBus = MessageBusManager.createMessageBus(settings, securityManager1);
        conversationMediator = new CollectionBasedConversationMediator(settings, messageBus);

        putClient = new ConversationBasedPutFileClient(
                messageBus,
                conversationMediator,
                settings, settings.getComponentID());
        getFileIDsClient = new ConversationBasedGetFileIDsClient(
                messageBus,
                conversationMediator,
                settings, settings.getComponentID());
        deleteFileClient = new ConversationBasedDeleteFileClient(
                messageBus,
                conversationMediator,
                settings, settings.getComponentID());
    }

    public void cleanCollection(Collection<String> pillarIDs) {
    }
}
