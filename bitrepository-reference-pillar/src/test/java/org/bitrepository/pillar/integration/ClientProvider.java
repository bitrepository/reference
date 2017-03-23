package org.bitrepository.pillar.integration;/*
 * #%L
 * Bitrepository Integrity Service
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

import org.bitrepository.access.getaudittrails.AuditTrailClientTestWrapper;
import org.bitrepository.access.getaudittrails.BlockingAuditTrailClient;
import org.bitrepository.access.getaudittrails.ConversationBasedAuditTrailClient;
import org.bitrepository.access.getchecksums.BlockingGetChecksumsClient;
import org.bitrepository.access.getchecksums.ConversationBasedGetChecksumsClient;
import org.bitrepository.access.getchecksums.GetChecksumsClientTestWrapper;
import org.bitrepository.access.getfileids.BlockingGetFileIDsClient;
import org.bitrepository.access.getfileids.ConversationBasedGetFileIDsClient;
import org.bitrepository.access.getfileids.GetFileIDsClientTestWrapper;
import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.modify.deletefile.BlockingDeleteFileClient;
import org.bitrepository.modify.deletefile.ConversationBasedDeleteFileClient;
import org.bitrepository.modify.deletefile.DeleteFileClientTestWrapper;
import org.bitrepository.modify.putfile.BlockingPutFileClient;
import org.bitrepository.modify.putfile.ConversationBasedPutFileClient;
import org.bitrepository.modify.putfile.PutFileClientTestWrapper;
import org.bitrepository.modify.replacefile.BlockingReplaceFileClient;
import org.bitrepository.modify.replacefile.ConversationBasedReplaceFileClient;
import org.bitrepository.modify.replacefile.ReplaceFileClientTestWrapper;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.jaccept.TestEventManager;

/**
 * Provides (blocking) reference client instances.
 */
public class ClientProvider {
    private final org.bitrepository.protocol.security.SecurityManager securityManager;
    private final Settings settings;
    private final TestEventManager eventManager;
    private final ConversationMediator conversationMediator;
    private final MessageBus messageBus;

    private BlockingPutFileClient putFileClient;
    private BlockingReplaceFileClient replaceFileClient;
    private BlockingDeleteFileClient getDeleteFileClient;
    private BlockingGetChecksumsClient getChecksumsClient;
    private BlockingGetFileIDsClient getFileIDsClient;
    private BlockingAuditTrailClient getAuditTrailsClient;

    /**
     * @param securityManager The security manager to use for the clients.
     * @param settings The settings to use for the clients.
     * @param eventManager
     */
    public ClientProvider(
        org.bitrepository.protocol.security.SecurityManager securityManager,
        Settings settings,
        TestEventManager eventManager) {
        this.securityManager = securityManager;
        this.settings = settings;
        this.eventManager = eventManager;

        messageBus = MessageBusManager.createMessageBus(settings, securityManager);
        conversationMediator = new CollectionBasedConversationMediator(settings, messageBus);
    }

    public synchronized BlockingPutFileClient getPutClient() {
        if (putFileClient == null) {
            putFileClient = new BlockingPutFileClient(
                new PutFileClientTestWrapper(
                        new ConversationBasedPutFileClient(
                                messageBus,
                                conversationMediator,
                                settings, settings.getComponentID()), eventManager
                )
            );
        }
        return putFileClient;
    }

    public synchronized BlockingReplaceFileClient getReplaceFileClient() {
        if (replaceFileClient == null) {
            replaceFileClient = new BlockingReplaceFileClient(
                    new ReplaceFileClientTestWrapper(
                            new ConversationBasedReplaceFileClient(
                                    messageBus,
                                    conversationMediator,
                                    settings, settings.getComponentID()), eventManager
                    )
            );
        }
        return replaceFileClient;
    }

    public synchronized BlockingDeleteFileClient getDeleteFileClient() {
        if (getDeleteFileClient == null) {
            getDeleteFileClient = new BlockingDeleteFileClient(
                new DeleteFileClientTestWrapper(
                        new ConversationBasedDeleteFileClient(
                                messageBus,
                                conversationMediator,
                                settings, settings.getComponentID()), eventManager
                )
            );
        }
        return getDeleteFileClient;
    }

    public synchronized BlockingGetChecksumsClient getGetChecksumsClient() {
        if (getChecksumsClient == null) {
            getChecksumsClient = new BlockingGetChecksumsClient(
                new GetChecksumsClientTestWrapper(
                        new ConversationBasedGetChecksumsClient(
                                messageBus,
                                conversationMediator,
                                settings, settings.getComponentID()), eventManager
                )
            );
        }
        return getChecksumsClient;
    }

    public synchronized BlockingGetFileIDsClient getGetFileIDsClient() {
        if (getFileIDsClient == null) {
            getFileIDsClient = new BlockingGetFileIDsClient(
                new GetFileIDsClientTestWrapper(
                        new ConversationBasedGetFileIDsClient(
                                messageBus,
                                conversationMediator,
                                settings, settings.getComponentID()), eventManager
                )
            );
        }
        return getFileIDsClient;
    }


    public synchronized BlockingAuditTrailClient getAuditTrailsClient() {
        if (getAuditTrailsClient == null) {
            getAuditTrailsClient = new BlockingAuditTrailClient(
                    new AuditTrailClientTestWrapper(
                            new ConversationBasedAuditTrailClient(
                                    settings,
                                    conversationMediator,
                                    messageBus,
                                    settings.getComponentID()), eventManager
                    )
            );
        }
        return getAuditTrailsClient;
    }
}
