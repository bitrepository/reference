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

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import javax.jms.Message;
import javax.jms.MessageListener;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.common.TestValidationUtils;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.activemq.ActiveMQMessageBus;
import org.bitrepository.protocol.message.ExampleMessageFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.jaccept.TestEventManager;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Class for testing the interface with the message bus.
 */
public class GeneralMessageBusTest extends IntegrationTest {

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
    public final void messageBusManagerTest() {
        addDescription("Verify the message bus manager");
        addStep("Test the extraction of the messagebus from the manager.", 
                "Null before it has been instantiated, and otherwise the same");
        settingsForCUT.getCollectionSettings().setCollectionID("A completely different id");
        Assert.assertNull(MessageBusManager.getMessageBus(settingsForCUT.getCollectionID()));
        MessageBus b1 = MessageBusManager.getMessageBus(settingsForCUT, securityManager);
        Assert.assertNotNull(b1);
        MessageBus b2 = MessageBusManager.getMessageBus(settingsForCUT.getCollectionID());
        Assert.assertNotNull(b2);
        Assert.assertEquals(b1, b2);
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
        addStep("Send a message with the 'Recipent' parameter set to at specific component",
                "The MESSAGE_RECIPIENT_KEY ");
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
        Assert.assertEquals(receivedMessage.getStringProperty(ActiveMQMessageBus.MESSAGE_RECIPIENT_KEY), receiverID);
    }

    @Test(groups = {"regressiontest"})
    public final void recipientFilterTest() throws Exception {
        addDescription("Test that message bus filters messages to other components, eg. ignores these.");
        addStep("Send an message with a undefined 'Receiver' header property, " +
                "eg. this messages should be handled by all components.",
                "Verify that the message bus accepts this message.");
        final BlockingQueue<Message> messageList = new LinkedBlockingDeque<Message>();
        messageBus.getComponentFilter().add(settingsForTestClient.getComponentID());
        RawMessagebus rawMessagebus = new RawMessagebus(
                settingsForTestClient.getMessageBusConfiguration(),
                securityManager);
        AlarmMessage messageToSend = ExampleMessageFactory.createMessage(AlarmMessage.class);
        messageToSend.setDestination(settingsForTestClient.getAlarmDestination());
        javax.jms.Message msg = rawMessagebus.createMessage(messageToSend);
        rawMessagebus.addHeader(msg, messageToSend.getClass().getSimpleName(), messageToSend.getReplyTo(),
                messageToSend.getCollectionID(),
                messageToSend.getCorrelationID());
        rawMessagebus.sendMessage(settingsForTestClient.getAlarmDestination(), msg);
        alarmReceiver.waitForMessage(AlarmMessage.class);

        addStep("Send an message with the 'Receiver' header property set to this component",
                "Verify that the message bus accepts this message.");
        msg.setStringProperty(ActiveMQMessageBus.MESSAGE_RECIPIENT_KEY, settingsForTestClient.getComponentID());
        rawMessagebus.sendMessage(settingsForTestClient.getAlarmDestination(), msg);
        alarmReceiver.waitForMessage(AlarmMessage.class);

        addStep("Send an invalid message with the 'Receiver' header property set to another specific component",
                "Verify that the message bus ignores this before parsing the message.");
        msg.setStringProperty(ActiveMQMessageBus.MESSAGE_RECIPIENT_KEY, "OtherComponent");
        rawMessagebus.sendMessage(settingsForTestClient.getAlarmDestination(), msg);
        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
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
