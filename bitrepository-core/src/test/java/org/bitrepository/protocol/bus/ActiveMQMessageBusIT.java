/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2015 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.protocol.bus;

import jakarta.jms.Message;
import jakarta.jms.MessageListener;
import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.protocol.CoordinationLayerException;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.message.ExampleMessageFactory;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.settings.repositorysettings.MessageBusConfiguration;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;


/**
 * Runs the GeneralMessageBusTest using a LocalActiveMQBroker and a suitable
 * MessageBus based on TestContainers.  Regression tests use Allure to generate reports.
 */
@Testcontainers
class ActiveMQMessageBusIT extends GeneralMessageBusIT {

    @Container
    static ArtemisContainer activemqContainer = new ArtemisContainer("apache/artemis:2.55.0")
                                                        .withEnv("ANONYMOUS_LOGIN", "true");

    private MessageBusConfiguration messageBusConfig;

    @Override
    protected void setupMessageBus() {
        while (!activemqContainer.isRunning()) {
            activemqContainer.start();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        messageBusConfig = new MessageBusConfiguration();
        messageBusConfig.setURL(activemqContainer.getBrokerUrl());
        messageBusConfig.setName(activemqContainer.getContainerName());
        settingsForTestClient.getRepositorySettings()
                             .getProtocolSettings()
                             .setMessageBusConfiguration(messageBusConfig);


        messageBus = new ActiveMQMessageBus(settingsForTestClient, securityManager);
        MessageBusManager.clear();
        MessageBusManager.injectCustomMessageBus(MessageBusManager.DEFAULT_MESSAGE_BUS, messageBus);
    }

    @Override
    public boolean useEmbeddedMessageBus() {
        return false;
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public final void collectionFilterTest() throws Exception {
        addDescription("Test that message bus filters identify requests to other collection, i.e. ignores these.");
        addStep("""
                Send an identify request with a undefined 'Collection' header property, \
                i.e. this identify requests should be handled by everybody.""",
                "Verify that the message bus accepts this message.");
        String myCollectionID = "MyCollection";
        messageBus.setCollectionFilter(List.of(myCollectionID));
        String collectionDestination = settingsForTestClient.getCollectionDestination();

        try (RawMessagebus rawMessagebus = new RawMessagebus(messageBusConfig)) {
            var identifyRequest = ExampleMessageFactory.createMessage(IdentifyPillarsForDeleteFileRequest.class);
            identifyRequest.setCollectionID(myCollectionID);
            jakarta.jms.Message msg = rawMessagebus.createMessage(identifyRequest);
            rawMessagebus.addHeader(msg,
                                    identifyRequest.getClass().getSimpleName(),
                                    identifyRequest.getReplyTo(),
                                    null,
                                    identifyRequest.getCorrelationID());

            rawMessagebus.sendMessage(collectionDestination, msg);
            collectionReceiver.waitForMessage(identifyRequest.getClass(), identifyRequest.getCorrelationID());

            addStep("Send an identify request with the 'Collection' header property set to my collection",
                    "Verify that the request bus accepts this message.");
            msg.setStringProperty(ActiveMQMessageBus.COLLECTION_ID_KEY, myCollectionID);
            rawMessagebus.sendMessage(collectionDestination, msg);
            collectionReceiver.waitForMessage(identifyRequest.getClass(), identifyRequest.getCorrelationID());

            addStep("Send an invalid message with the 'Receiver' header property set to another specific component",
                    "Verify that the message bus ignores this before parsing the message.");
            msg.setStringProperty(ActiveMQMessageBus.COLLECTION_ID_KEY, "OtherCollection");
            rawMessagebus.sendMessage(collectionDestination, msg);
            collectionReceiver.checkNoMessageIsReceived(identifyRequest.getClass());
        }
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public final void sendMessageToSpecificComponentTest() throws Exception {
        addDescription("Test that message bus correct uses the 'to' header property to indicated that the message " +
                       "is meant for a specific component");
        addStep("Send a message with the 'Recipient' parameter set to at specific component",
                "The MESSAGE_TO_KEY ");
        String receiverID = "specificReceiver";
        final BlockingQueue<Message> messageList = new LinkedBlockingDeque<>();
        try (RawMessagebus rawMessagebus = new RawMessagebus(messageBusConfig)) {
            rawMessagebus.addListener(settingsForTestClient.getCollectionDestination(),
                                      (MessageListener) messageList::add);
            IdentifyPillarsForDeleteFileRequest messageToSend =
                    ExampleMessageFactory.createMessage(IdentifyPillarsForDeleteFileRequest.class);
            messageToSend.setDestination(settingsForTestClient.getCollectionDestination());
            messageToSend.setTo(receiverID);
            messageBus.sendMessage(messageToSend);
            Message receivedMessage = messageList.poll(3, TimeUnit.SECONDS);
            Assertions.assertNotNull(receivedMessage);
            Assertions.assertEquals(receiverID, receivedMessage.getStringProperty(ActiveMQMessageBus.MESSAGE_TO_KEY));
        }
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public final void toFilterTest() throws Exception {
        addDescription("Test that message bus filters identify requests to other components, i.e. ignores these.");
        addStep("Send an identify request with a undefined 'To' header property, " +
                "i.e. this identify requests should be handled by all components.",
                "Verify that the identify request bus accepts this identify request.");
        messageBus.setComponentFilter(Collections.singletonList(settingsForTestClient.getComponentID()));
        String collectionDestination = settingsForTestClient.getCollectionDestination();

        try (RawMessagebus rawMessagebus = new RawMessagebus(messageBusConfig)) {
            var identifyRequest = ExampleMessageFactory.createMessage(IdentifyPillarsForDeleteFileRequest.class);

            identifyRequest.setDestination(collectionDestination);
            Message msg = rawMessagebus.createMessage(identifyRequest);
            rawMessagebus.addHeader(msg,
                                    identifyRequest.getClass().getSimpleName(),
                                    identifyRequest.getReplyTo(),
                                    null,
                                    identifyRequest.getCorrelationID());
            rawMessagebus.sendMessage(collectionDestination, msg);
            collectionReceiver.waitForMessage(identifyRequest.getClass(), identifyRequest.getCorrelationID());

            addStep("Send an identify request with the 'To' header property set to this component",
                    "Verify that the identify request bus accepts this identify request.");
            msg.setStringProperty(ActiveMQMessageBus.MESSAGE_TO_KEY, settingsForTestClient.getComponentID());
            rawMessagebus.sendMessage(collectionDestination, msg);
            collectionReceiver.waitForMessage(identifyRequest.getClass(), identifyRequest.getCorrelationID());

            addStep("Send an invalid identify request with the 'To' header property set to another specific component",
                    "Verify that the identify request bus ignores this before parsing the identify request.");
            msg.setStringProperty(ActiveMQMessageBus.MESSAGE_TO_KEY, "OtherComponent");
            rawMessagebus.sendMessage(collectionDestination, msg);
            collectionReceiver.checkNoMessageIsReceived(identifyRequest.getClass());

            addStep("Send an identify response with the 'To' header property set to another component",
                    "Verify that the message bus accepts this message.");
            var identifyResponse = ExampleMessageFactory.createMessage(IdentifyPillarsForDeleteFileResponse.class);
            identifyRequest.setDestination(collectionDestination);
            Message response = rawMessagebus.createMessage(identifyResponse);
            rawMessagebus.addHeader(response,
                                    identifyResponse.getClass().getSimpleName(),
                                    identifyResponse.getReplyTo(),
                                    null,
                                    identifyRequest.getCorrelationID());
            response.setStringProperty(ActiveMQMessageBus.MESSAGE_TO_KEY, "OtherComponent");
            rawMessagebus.sendMessage(collectionDestination, response);
            collectionReceiver.waitForMessage(identifyResponse.getClass(), identifyRequest.getCorrelationID());

            addStep("Send an non-identify request with the 'To' header property set to another component",
                    "Verify that the message bus accepts this message.");
            var deleteFileRequest = ExampleMessageFactory.createMessage(DeleteFileRequest.class);
            deleteFileRequest.setDestination(collectionDestination);
            var deleteFileRequestMessage = rawMessagebus.createMessage(deleteFileRequest);
            rawMessagebus.addHeader(deleteFileRequestMessage,
                                    deleteFileRequest.getClass().getSimpleName(),
                                    deleteFileRequest.getReplyTo(),
                                    null,
                                    identifyRequest.getCorrelationID());
            response.setStringProperty(ActiveMQMessageBus.MESSAGE_TO_KEY, "OtherComponent");
            rawMessagebus.sendMessage(collectionDestination, deleteFileRequestMessage);
            collectionReceiver.waitForMessage(deleteFileRequest.getClass(), identifyRequest.getCorrelationID());
        }
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public final void closeReleasesJmsResourcesTest() throws Exception {
        try {
            addDescription("Test that closing a message bus releases its JMS resources, so it can no longer " +
                           "be used to send messages afterwards.");
            addStep("Create a dedicated message bus instance and close it",
                    "No exception should be thrown while closing.");

            messageBus.close();

            addStep("Attempt to send a message on the closed message bus",
                    "The send should fail since the underlying JMS session and connection have been closed.");
            IdentifyPillarsForDeleteFileRequest message =
                    ExampleMessageFactory.createMessage(IdentifyPillarsForDeleteFileRequest.class);
            message.setDestination(settingsForTestClient.getCollectionDestination());
            Assertions.assertThrows(CoordinationLayerException.class, () -> messageBus.sendMessage(message));
        } finally {
            setupMessageBus();
        }
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    public final void rawMessagebusCloseReleasesJmsResourcesTest() throws Exception {
        addDescription("Test that closing a RawMessagebus releases its JMS resources, so it can no longer " +
                       "be used afterwards.");
        addStep("Create a raw message bus instance and close it",
                "No exception should be thrown while closing.");
        RawMessagebus rawMessagebus = new RawMessagebus(messageBusConfig);
        rawMessagebus.close();

        addStep("Attempt to create a producer on the closed raw message bus",
                "The call should fail, since the underlying JMS session and connection have been closed.");
        Assertions.assertThrows(CoordinationLayerException.class,
                                () -> rawMessagebus.getProducer(settingsForTestClient.getCollectionDestination()));
    }

}
