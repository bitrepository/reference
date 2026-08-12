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

import io.qameta.allure.Description;
import org.bitrepository.SuiteInfoParameterResolver;
import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.message.ExampleMessageFactory;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import jakarta.jms.JMSException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

/**
 * Class for testing the interface with the message bus.
 */
@ExtendWith(SuiteInfoParameterResolver.class)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
abstract class GeneralMessageBusIT extends IntegrationTest {
    /**
     * The time to wait when sending a message before it definitely should
     * have been consumed by a listener.
     */
    static final Duration TIME_FOR_WAIT = Duration.ofMillis(2500);
    private final static int threadCount = 3;

    protected MessageReceiver collectionReceiver;

    protected static Logger log = LoggerFactory.getLogger(GeneralMessageBusIT.class);

    private int count = 0;
    private final static String FINISH = "FINISH";
    private final BlockingQueue<String> finishQueue = new LinkedBlockingQueue<>(1);
    MultiMessageListener listener;


    @Override
    protected void registerMessageReceivers() {
        super.registerMessageReceivers();
        collectionReceiver = addReceiver(new MessageReceiver(settingsForCUT.getCollectionDestination()));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    @Description("""
                 Tests whether it is possible to create a message listener, \
                 and then set it to listen to the topic. Then puts a message \
                 on the topic for the message listener to find, and \
                 tests whether it finds the correct message.""")
    final void busActivityTest() throws Exception {

        addStep("Send a message to the topic", "No exceptions should be thrown");
        AlarmMessage message = ExampleMessageFactory.createMessage(AlarmMessage.class);
        message.setDestination(alarmDestinationID);
        messageBus.sendMessage(message);

        addStep("Wait for the message to be received",
                "A message should be received");
        alarmReceiver.waitForMessage(message.getClass());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    final void twoListenersForTopicTest() throws Exception {
        addDescription("Verifies that two listeners on the same topic both receive the message");

        addStep("Make a connection to the message bus and add two listeners",
                "No exceptions should be thrown");
        MessageReceiver receiver1 = new MessageReceiver(alarmDestinationID);
        addReceiver(receiver1);
        messageBus.addListener(receiver1.getDestination(), receiver1.getMessageListener());
        MessageReceiver receiver2 = new MessageReceiver(alarmDestinationID);
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

    @Test
    @Tag(TestGroups.SPECIFICATIONONLY)
    final void messageBusFailoverTest() {
        addDescription("Verifies that we can switch to at second message bus " +
                       "in the middle of a conversation, if the connection is lost. " +
                       "We should also be able to resume the conversation on the new " +
                       "message bus");
    }

    @Test
    @Tag(TestGroups.SPECIFICATIONONLY)
    final void messageBusReconnectTest() {
        addDescription("Test whether we are able to reconnect to the message " +
                       "bus if the connection is lost");
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    final void manyThreadsBeforeFinish() throws Exception {
        addDescription("Tests whether it is possible to start the handling of many threads simultaneously.");
        var identifyRequest = ExampleMessageFactory.createMessage(IdentifyPillarsForGetFileRequest.class);
        listener = new MultiMessageListener();
        messageBus.addListener("BusActivityTest", listener);
        identifyRequest.setDestination("BusActivityTest");

        addStep("Send one message for each listener",
                "When all have receiver, then they give respond on 'finishQueue'");
        for (int i = 0; i < threadCount; i++) {
            messageBus.sendMessage(identifyRequest);
        }
        Assertions.assertEquals(FINISH, finishQueue.poll(TIME_FOR_WAIT.toMillis(), TimeUnit.MILLISECONDS));
        Assertions.assertEquals(threadCount, count);
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
                log.warn("Failed to close messageBus", e);
            }
        }
    }


    protected class MultiMessageListener implements MessageListener {
        private final BlockingQueue<String> queue = new LinkedBlockingQueue<>(threadCount);

        @Override
        public final void onMessage(Message message, MessageContext messageContext) {
            try {
                testIfFinished();
                Assertions.assertNotNull(queue.poll(TIME_FOR_WAIT.toMillis(), TimeUnit.MILLISECONDS));
            } catch (InterruptedException e) {
                Assertions.fail("Should not throw an exception: ", e);
            }
        }

        private void testIfFinished() throws InterruptedException {
            count++;
            if (count >= threadCount) {
                for (int i = 0; i < threadCount; i++) {
                    queue.put("Count '" + i + "'");
                }
                finishQueue.put(FINISH);
            }
        }
    }
}
