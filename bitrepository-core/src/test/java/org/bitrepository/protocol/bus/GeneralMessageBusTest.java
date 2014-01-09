/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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

import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.common.TestValidationUtils;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.message.ExampleMessageFactory;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.jaccept.TestEventManager;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import javax.jms.Message;
import javax.jms.MessageListener;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

/**
 * Class for testing the interface with the message bus.
 */
public class GeneralMessageBusTest extends IntegrationTest {
    protected static MessageReceiver collectionReceiver;

    @Override
    protected void registerMessageReceivers() {
        super.registerMessageReceivers();
        collectionReceiver = new MessageReceiver(settingsForCUT.getCollectionDestination(), testEventManager);
        addReceiver(collectionReceiver);
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        messageBus.setComponentFilter(Arrays.asList(new String[]{}));
        messageBus.setCollectionFilter(Arrays.asList(new String[]{}));
    }

    @Test(groups = { "regressiontest" })
    public void utilityTester() throws Exception {
        addDescription("Test that the utility class is a proper utility class.");
        TestValidationUtils.validateUtilityClass(MessageBusManager.class);
    }

    @Test(groups = { "regressiontest" })
    public final void messageBusConnectionTest() {
        addDescription("Verifies that we are able to connect to the message bus");
        addStep("Get a connection to the message bus from the "
                + "<i>MessageBusConnection</i> connection class",
                "No exceptions should be thrown");
        Assert.assertNotNull(ProtocolComponentFactory.getInstance().getMessageBus(settingsForCUT, securityManager));
    }

    @Test(groups = { "regressiontest" })
    public final void busActivityTest() throws Exception {
        addDescription("Tests whether it is possible to create a message listener, " +
                "and then set it to listen to the topic. Then puts a message" +
                "on the topic for the message listener to find, and" +
                "tests whether it finds the correct message.");

        addStep("Send a message to the topic", "No exceptions should be thrown");
        AlarmMessage message = ExampleMessageFactory.createMessage(AlarmMessage.class);
        message.setDestination(alarmDestinationID);
        messageBus.sendMessage(message);

        addStep("Make sure both listeners received the message",
                "Both listeners received the message, and it is identical");
        alarmReceiver.waitForMessage(message.getClass());
    }

    @Test(groups = {"regressiontest"})
    public final void twoListenersForTopicTest() throws Exception {
        addDescription("Verifies that two listeners on the same topic both receive the message");
        TestEventManager testEventManager = TestEventManager.getInstance();

        addStep("Make a connection to the message bus and add two listeners", "No exceptions should be thrown");
        MessageReceiver receiver1 = new MessageReceiver(alarmDestinationID, testEventManager);
        addReceiver(receiver1);
        messageBus.addListener(receiver1.getDestination(), receiver1.getMessageListener());
        MessageReceiver receiver2 = new MessageReceiver(alarmDestinationID, testEventManager);
        addReceiver(receiver2);
        messageBus.addListener(receiver2.getDestination(), receiver2.getMessageListener());

        addStep("Send a message to the topic", "No exceptions should be thrown");
        AlarmMessage message = ExampleMessageFactory.createMessage(AlarmMessage.class);
        message.setDestination(alarmDestinationID);
        messageBus.sendMessage(message);

        addStep("Make sure both listeners received the message",
                "Both listeners received the message, and it is identical");
        receiver1.waitForMessage(AlarmMessage.class);
        receiver2.waitForMessage(AlarmMessage.class);
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
        Assert.assertEquals(receivedMessage.getStringProperty(ActiveMQMessageBus.MESSAGE_TO_KEY), receiverID);
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

    @Test(groups = {"regressiontest"})
    public final void collectionFilterTest() throws Exception {
        addDescription("Test that message bus filters identify requests to other collection, eg. ignores these.");
        addStep("Send an identify request with a undefined 'Collection' header property, " +
                "eg. this identify requests should be handled by everybody.",
                "Verify that the message bus accepts this message.");
        String myCollectionID = "MyCollection";
        messageBus.setCollectionFilter(Arrays.asList(new String[] { myCollectionID }));
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

    @Test(groups = { "specificationonly" })
    public final void messageBusFailoverTest() {
        addDescription("Verifies that we can switch to at second message bus " +
                "in the middle of a conversation, if the connection is lost. " +
                "We should also be able to resume the conversation on the new " +
                "message bus");
    }

    @Test(groups = { "specificationonly" })
    public final void messageBusReconnectTest() {
        addDescription("Test whether we are able to reconnect to the message " +
                "bus if the connection is lost");
    }
}
