/*
 * #%L
 * Bitrepository Protocol
 *
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
package org.bitrepository.client;

import jakarta.jms.JMSException;
import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.pillar.integration.ArtemisFixedPortContainer;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.bitrepository.protocol.message.ClientTestMessageFactory;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.TestInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Contains the generic parts for tests integrating to the message bus.
 */
@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class DefaultFixtureClientIT extends IntegrationTest {
    protected static final String DEFAULT_FILE_ID = ClientTestMessageFactory.FILE_ID_DEFAULT;
    private static final Logger log = LoggerFactory.getLogger(DefaultFixtureClientIT.class);
    protected static MessageReceiver collectionReceiver;

    protected static String pillar1DestinationId;
    protected static MessageReceiver pillar1Receiver;
    protected static final String PILLAR1_ID = "Pillar1";

    protected static String pillar2DestinationId;
    protected static MessageReceiver pillar2Receiver;
    protected static final String PILLAR2_ID = "Pillar2";

    protected ConversationMediator conversationMediator;

    @Container
    static ArtemisContainer activemqContainer = new ArtemisFixedPortContainer("apache/artemis:2.55.0")
                                                            .withFixedExposedPort(9999, 61616, InternetProtocol.TCP)
                                                            .withEnv("ANONYMOUS_LOGIN","true");

    @Override
    protected void initializeCUT() {
        super.initializeCUT();
        renewConversationMediator();
    }

    protected void registerMessageReceivers() {
        super.registerMessageReceivers();
        collectionReceiver = addReceiver(new MessageReceiver(settingsForCUT.getCollectionDestination()));

        pillar1DestinationId = "Pillar1_topic" + getTopicPostfix();
        pillar1Receiver = addReceiver(new MessageReceiver(pillar1DestinationId));

        pillar2DestinationId = "Pillar2_topic" + getTopicPostfix();
        pillar2Receiver = addReceiver(new MessageReceiver(pillar2DestinationId));
    }


    /**
     * Used for creating a new conversationMediator between tests, and for tests needing to use a differently configured
     * mediator.
     */
    protected void renewConversationMediator() {
        if (conversationMediator != null) {
            conversationMediator.shutdown();
        }
        conversationMediator = new CollectionBasedConversationMediator(settingsForCUT, securityManager);
    }

    /**
     * Hooks up the message bus.
     */
    protected void setupMessageBus() {
        activemqContainer.start();
        while (!activemqContainer.isRunning()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        settingsForTestClient.getMessageBusConfiguration().setURL(activemqContainer.getBrokerUrl());
        messageBus = new ActiveMQMessageBus(settingsForTestClient, securityManager);
        MessageBusManager.clear();
        MessageBusManager.injectCustomMessageBus(MessageBusManager.DEFAULT_MESSAGE_BUS, messageBus);
    }

    /**
     * Shutdown the message bus.
     */
    @Override
    protected void teardownMessageBus() {
        MessageBusManager.clear();
        if (messageBus != null) {
            try {
                messageBus.close();
                messageBus = null;
            } catch (JMSException e) {
                log.warn("Failed when close the messageBus", e);
            }
            activemqContainer.stop();
        }
    }
}
