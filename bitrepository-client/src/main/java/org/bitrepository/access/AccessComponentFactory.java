/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access;

import org.bitrepository.access.getaudittrails.AuditTrailClient;
import org.bitrepository.access.getaudittrails.ConversationBasedAuditTrailClient;
import org.bitrepository.access.getchecksums.ConversationBasedGetChecksumsClient;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getfile.ConversationBasedGetFileClient;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.access.getfileids.ConversationBasedGetFileIDsClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.access.getstatus.ConversationBasedGetStatusClient;
import org.bitrepository.access.getstatus.GetStatusClient;
import org.bitrepository.client.conversation.mediator.ConversationMediatorManager;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.security.SecurityManager;

public final class AccessComponentFactory {
    private static AccessComponentFactory instance;

    /**
     * Instantiation of the class as a singleton.
     *
     * @return The singleton instance of this factory class.
     */
    public static synchronized AccessComponentFactory getInstance() {
        if (instance == null) {
            instance = new AccessComponentFactory();
        }
        return instance;
    }

    private AccessComponentFactory() {
    }

    /**
     * Method for getting a GetFileClient as defined in the access configuration.<p>
     *
     * @param settings        The settings for the GetFileClient.
     * @param securityManager The SecurityManager for the client
     * @param clientID        The ID of the client
     * @return A GetFileClient created form the given arguments.
     */
    public GetFileClient createGetFileClient(Settings settings, SecurityManager securityManager, String clientID) {
        return new ConversationBasedGetFileClient(
                ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager),
                ConversationMediatorManager.getConversationMediator(settings, securityManager),
                settings, clientID);
    }

    /**
     * Returns a GetChecksumsClient as defined in the access configurations.
     *
     * @param settings        The settings for the GetChecksumsClient.
     * @param securityManager The SecurityManager for the client
     * @param clientID        The ID of the client
     * @return The GetChecksumsClient created from the given arguments.
     */
    public GetChecksumsClient createGetChecksumsClient(Settings settings, SecurityManager securityManager, String clientID) {
        return new ConversationBasedGetChecksumsClient(
                ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager),
                ConversationMediatorManager.getConversationMediator(settings, securityManager),
                settings, clientID);
    }

    /**
     * Instantiates a GetFileIDsClient as defined in the access configuration.<p>
     *
     * @param settings        The settings for the GetFileIDsClient.
     * @param securityManager The SecurityManager for the client
     * @param clientID        The ID of the client
     * @return A GetFileIDsClient created from the given arguments.
     */
    public GetFileIDsClient createGetFileIDsClient(Settings settings, SecurityManager securityManager, String clientID) {
        return new ConversationBasedGetFileIDsClient(
                ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager),
                ConversationMediatorManager.getConversationMediator(settings, securityManager),
                settings, clientID);
    }

    /**
     * Instantiates a GetStatusClient as defined in the access configuration.<p>
     *
     * @param settings        The settings for the GetStatusClient.
     * @param securityManager The SecurityManager for the client
     * @param clientID        The ID of the client
     * @return A GetStatusClient created from the given arguments.
     */
    public GetStatusClient createGetStatusClient(Settings settings, SecurityManager securityManager, String clientID) {
        return new ConversationBasedGetStatusClient(
                ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager),
                ConversationMediatorManager.getConversationMediator(settings, securityManager),
                settings, clientID);
    }

    /**
     * Instantiates a AuditTrailClient as defined in the access configuration.<p>
     *
     * @param settings        The settings for the AuditTrailClient.
     * @param securityManager The SecurityManager for the client
     * @param clientID        The ID of the client
     * @return A AuditTrailClient created from the given arguments.
     */
    public AuditTrailClient createAuditTrailClient(Settings settings, SecurityManager securityManager, String clientID) {
        return new ConversationBasedAuditTrailClient(
                settings, ConversationMediatorManager.getConversationMediator(settings, securityManager),
                ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager),
                clientID);
    }
}
