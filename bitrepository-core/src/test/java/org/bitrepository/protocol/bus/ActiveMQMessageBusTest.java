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

import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.message.ExampleMessageFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;

/**
 * Runs the GeneralMessageBusTest using a LocalActiveMQBroker (if useEmbeddedMessageBus is true) and a suitable
 * MessageBus based on settingsForTestClient.  Regression tests utilized that uses JAccept to generate reports.
 */

public class ActiveMQMessageBusTest extends GeneralMessageBusTest {

    @BeforeClass(alwaysRun = true)
    @Override
    public void initMessagebus() {
        if (useEmbeddedMessageBus() && broker == null) {
            broker = new LocalActiveMQBroker(settingsForTestClient.getMessageBusConfiguration());
            broker.start();
        }
        MessageBus messageBus = MessageBusManager.createMessageBus(settingsForTestClient, securityManager);
        this.messageBus = new MessageBusWrapper(messageBus, testEventManager);

    }

    @Test(groups = {"regressiontest"})
    public final void collectionFilterTest() throws Exception {
        addDescription("Test that message bus filters identify requests to other collection, eg. ignores these.");
        addStep("Send an identify request with a undefined 'Collection' header property, " +
                        "eg. this identify requests should be handled by everybody.",
                "Verify that the message bus accepts this message.");
        String myCollectionID = "MyCollection";
        messageBus.setCollectionFilter(Arrays.asList(new String[]{myCollectionID}));
        RawMessagebus rawMessagebus = new RawMessagebus(
                settingsForTestClient.getMessageBusConfiguration(),
                securityManager);
        IdentifyPillarsForDeleteFileRequest identifyRequest =
                ExampleMessageFactory.createMessage(IdentifyPillarsForDeleteFileRequest.class);
        identifyRequest.setCollectionID(myCollectionID);
        javax.jms.Message msg = rawMessagebus.createMessage(identifyRequest);
        rawMessagebus.addHeader(msg, identifyRequest.getClass().getSimpleName(), identifyRequest.getReplyTo(),
                null,
                identifyRequest.getCorrelationID());
        rawMessagebus.sendMessage(settingsForTestClient.getCollectionDestination(), msg);
        collectionReceiver.waitForMessage(IdentifyPillarsForDeleteFileRequest.class);

        addStep("Send an identify request with the 'Collection' header property set to my collection",
                "Verify that the request bus accepts this message.");
        msg.setStringProperty(ActiveMQMessageBus.COLLECTION_ID_KEY, myCollectionID);
        rawMessagebus.sendMessage(settingsForTestClient.getCollectionDestination(), msg);
        collectionReceiver.waitForMessage(IdentifyPillarsForDeleteFileRequest.class);

        addStep("Send an invalid message with the 'Receiver' header property set to another specific component",
                "Verify that the message bus ignores this before parsing the message.");
        msg.setStringProperty(ActiveMQMessageBus.COLLECTION_ID_KEY, "OtherCollection");
        rawMessagebus.sendMessage(settingsForTestClient.getCollectionDestination(), msg);
        collectionReceiver.checkNoMessageIsReceived(IdentifyPillarsForDeleteFileRequest.class);
    }

    @Test(groups = {"regressiontest"})
    public final void sendMessageToSpecificComponentTest() throws Exception {
        addDescription("Test that message bus correct uses the 'to' header property to indicated that the message " +
                "is meant for a specific component");
        addStep("Send a message with the 'Recipient' parameter set to at specific component",
                "The MESSAGE_TO_KEY ");
        String receiverID = "specificReceiver";
        final BlockingQueue<Message> messageList = new LinkedBlockingDeque<Message>();
        RawMessagebus rawMessagebus = new RawMessagebus(
                settingsForTestClient.getMessageBusConfiguration(),
                securityManager);
        rawMessagebus.addListener(settingsForTestClient.getCollectionDestination(), new MessageListener() {
            @Override
            public void onMessage(Message message) {
                messageList.add(message);
            }
        });
        IdentifyPillarsForDeleteFileRequest messageToSend =
                ExampleMessageFactory.createMessage(IdentifyPillarsForDeleteFileRequest.class);
        messageToSend.setDestination(settingsForTestClient.getCollectionDestination());
        messageToSend.setTo(receiverID);
        messageBus.sendMessage(messageToSend);
        Message receivedMessage = messageList.poll(3, TimeUnit.SECONDS);
        assertEquals(receivedMessage.getStringProperty(ActiveMQMessageBus.MESSAGE_TO_KEY), receiverID);
    }

    @Test(groups = {"regressiontest"})
    public final void toFilterTest() throws Exception {
        addDescription("Test that message bus filters identify requests to other components, eg. ignores these.");
        addStep("Send an identify request with a undefined 'To' header property, " +
                        "eg. this identify requests should be handled by all components.",
                "Verify that the identify request bus accepts this identify request.");
        messageBus.setComponentFilter(Arrays.asList(new String[]{ settingsForTestClient.getComponentID() }));
        RawMessagebus rawMessagebus = new RawMessagebus(
                settingsForTestClient.getMessageBusConfiguration(),
                securityManager);
        IdentifyPillarsForDeleteFileRequest identifyRequest =
                ExampleMessageFactory.createMessage(IdentifyPillarsForDeleteFileRequest.class);
        identifyRequest.setDestination(settingsForTestClient.getCollectionDestination());
        javax.jms.Message msg = rawMessagebus.createMessage(identifyRequest);
        rawMessagebus.addHeader(msg, identifyRequest.getClass().getSimpleName(), identifyRequest.getReplyTo(),
                null,
                identifyRequest.getCorrelationID());
        rawMessagebus.sendMessage(settingsForTestClient.getCollectionDestination(), msg);
        collectionReceiver.waitForMessage(IdentifyPillarsForDeleteFileRequest.class);

        addStep("Send an identify request with the 'To' header property set to this component",
                "Verify that the identify request bus accepts this identify request.");
        msg.setStringProperty(ActiveMQMessageBus.MESSAGE_TO_KEY, settingsForTestClient.getComponentID());
        rawMessagebus.sendMessage(settingsForTestClient.getCollectionDestination(), msg);
        collectionReceiver.waitForMessage(IdentifyPillarsForDeleteFileRequest.class);

        addStep("Send an invalid identify request with the 'To' header property set to another specific component",
                "Verify that the identify request bus ignores this before parsing the identify request.");
        msg.setStringProperty(ActiveMQMessageBus.MESSAGE_TO_KEY, "OtherComponent");
        rawMessagebus.sendMessage(settingsForTestClient.getCollectionDestination(), msg);
        collectionReceiver.checkNoMessageIsReceived(IdentifyPillarsForDeleteFileRequest.class);

        addStep("Send an identify response with the 'To' header property set to another component",
                "Verify that the message bus accepts this message.");
        IdentifyPillarsForDeleteFileResponse identifyResponse =
                ExampleMessageFactory.createMessage(IdentifyPillarsForDeleteFileResponse.class);
        identifyRequest.setDestination(settingsForTestClient.getCollectionDestination());
        javax.jms.Message response = rawMessagebus.createMessage(identifyResponse);
        rawMessagebus.addHeader(response, identifyResponse.getClass().getSimpleName(), identifyResponse.getReplyTo(),
                null,
                identifyRequest.getCorrelationID());
        response.setStringProperty(ActiveMQMessageBus.MESSAGE_TO_KEY, "OtherComponent");
        rawMessagebus.sendMessage(settingsForTestClient.getCollectionDestination(), response);
        collectionReceiver.waitForMessage(IdentifyPillarsForDeleteFileResponse.class);

        addStep("Send an non-identify request with the 'To' header property set to another component",
                "Verify that the message bus accepts this message.");
        DeleteFileRequest request =
                ExampleMessageFactory.createMessage(DeleteFileRequest.class);
        request.setDestination(settingsForTestClient.getCollectionDestination());
        javax.jms.Message rq = rawMessagebus.createMessage(request);
        rawMessagebus.addHeader(rq, request.getClass().getSimpleName(), request.getReplyTo(),
                null,
                identifyRequest.getCorrelationID());
        response.setStringProperty(ActiveMQMessageBus.MESSAGE_TO_KEY, "OtherComponent");
        rawMessagebus.sendMessage(settingsForTestClient.getCollectionDestination(), rq);
        collectionReceiver.waitForMessage(DeleteFileRequest.class);
    }
}
