/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.service;

import java.math.BigInteger;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

/**
 */
public abstract class ContributerTest extends IntegrationTest {
    protected static final String DEFAULT_FILE_ID = ClientTestMessageFactory.FILE_ID_DEFAULT;

    protected static String clientDestinationId;
    protected MessageReceiver clientTopic;

    protected static String contributorDestinationId;
    protected MessageReceiver contributorDestination;

    protected static final BigInteger defaultTime = BigInteger.valueOf(3000);

    @Override
    protected void teardownMessageBusListeners() {
        messageBus.removeListener(clientDestinationId, clientTopic.getMessageListener());
        messageBus.removeListener(contributorDestinationId, contributorDestination.getMessageListener());
        super.teardownMessageBusListeners();
    }

    @Override
    protected void initializeMessageBusListeners() {
        super.initializeMessageBusListeners();
        clientDestinationId = componentSettings.getReceiverDestinationID();
        clientTopic = new MessageReceiver("Client topic receiver", testEventManager);

        contributorDestinationId =  collectionDestinationID + "-" +  getContributorID() + "-" + getTopicPostfix();
        contributorDestination = new MessageReceiver(contributorDestinationId + " topic receiver", testEventManager);
        messageBus.addListener(clientDestinationId, clientTopic.getMessageListener());
        messageBus.addListener(contributorDestinationId, contributorDestination.getMessageListener());
    }

    protected abstract String getContributorID();
}
