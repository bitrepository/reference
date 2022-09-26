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
package org.bitrepository.client.conversation.mediator;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.client.conversation.Conversation;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.DefaultThreadFactory;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.common.utils.XmlUtils;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ThreadFactory;

/**
 * Conversation handler that delegates messages to registered conversations.
 */
public class CollectionBasedConversationMediator implements ConversationMediator {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Map<String, Conversation> conversations;
    private final Settings settings;
    private static final boolean TIMER_IS_DAEMON = true;
    private static final String NAME_OF_TIMER = "Collection based conversation timer";
    /**
     * The timer used to schedule cleaning of conversations.
     *
     * @see ConversationCleaner
     */
    private static final Timer cleanTimer = new Timer(NAME_OF_TIMER, TIMER_IS_DAEMON);
    private final MessageBus messagebus;
    private static final ThreadFactory threadFactory = new DefaultThreadFactory(
            CollectionBasedConversationMediator.class.getSimpleName() + "-", Thread.NORM_PRIORITY, false);

    @Override
    public void start() {
        messagebus.addListener(settings.getReceiverDestinationID(), this);
        javax.xml.datatype.Duration cleanupInterval = settings.getReferenceSettings().getClientSettings().getMediatorCleanupInterval();
        cleanTimer.scheduleAtFixedRate(new ConversationCleaner(),
                0, XmlUtils.xmlDurationToMilliseconds(cleanupInterval));
    }

    @Override
    public void shutdown() {
        messagebus.removeListener(settings.getReceiverDestinationID(), this);
    }

    /**
     * Create a mediator that handles conversations and mediates messages sent on the
     * given destination on the given messageBus.
     *
     * @param settings        The general client settings.
     * @param securityManager Used by the message bus to authenticate messages.
     */
    public CollectionBasedConversationMediator(Settings settings, SecurityManager securityManager) {
        log.debug("Initializing the CollectionBasedConversationMediator");
        this.conversations = Collections.synchronizedMap(new HashMap<>());
        this.settings = settings;
        messagebus = MessageBusManager.getMessageBus(settings, securityManager);
        start();
    }

    @Override
    public void addConversation(Conversation conversation) {
        conversations.put(conversation.getConversationID(), conversation);
    }

    /**
     * Will try to fail a conversation gracefully. This consists of:
     * <ul>
     * <li> Removing the conversation from the list of conversations.
     * <li> Attempt to call the failConversation operation on the conversation. The call is made in a separate thread to
     * avoid having the failing conversation blocking the calling thread.
     * </ul>
     *
     * @param conversation The conversation to fail.
     * @param message      A message describing the failure symptoms.
     */
    private void failConversation(final Conversation conversation, final String message) {
        String conversationID = conversation.getConversationID();

        if (conversationID != null) {
            conversations.remove(conversationID);
            Thread t = threadFactory.newThread(new FailingConversation(conversation, message));
            t.start();
        }
    }

    /**
     * @param message The message with the unknown conversation ID.
     */
    private void handleUnknownConversation(Message message) {
        log.debug(message.getClass().getSimpleName() + " from " + message.getFrom() + " with correlationID '" + message.getCorrelationID() +
                "' could not be delegated to any " + "conversation.");
    }

    @Override
    public void onMessage(Message message, MessageContext messageContext) {
        String messageCorrelationID = message.getCorrelationID();
        Conversation conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message, messageContext);
        } else {
            handleUnknownConversation(message);
        }
    }

    /**
     * Will clean out obsolete conversations in each run. An obsolete conversation is a conversation which satisfies one
     * of the following criteria: <ol>
     * <li> Returns true for the <code>hasEnded()</code> method.
     * <li> Is older than the conversationTimeout limit allows.
     * </ol>
     * <p>
     * A copy of the current conversations is created before running through the conversations to avoid having to lock
     * the conversations map while cleaning.
     */
    private final class ConversationCleaner extends TimerTask {
        @Override
        public void run() {
            Conversation[] conversationArray = conversations.values().toArray(new Conversation[0]);
            Duration conversationTimeout = XmlUtils.xmlDurationToDuration(
                    settings.getReferenceSettings().getClientSettings().getConversationTimeout());
            Instant currentTime = Instant.now();
            for (Conversation conversation : conversationArray) {
                if (conversation.hasEnded()) {
                    conversations.remove(conversation.getConversationID());
                } else {
                    Instant startTime = Instant.ofEpochMilli(conversation.getStartTime());
                    Instant expirationTime = startTime.plus(conversationTimeout);
                    if (expirationTime.isBefore(currentTime)) {
                        log.warn("Failing timed out conversation {} (Age: {})", conversation.getConversationID(),
                                TimeUtils.durationToHuman(Duration.between(startTime, currentTime)));
                        failConversation(conversation, "Failing timed out conversation " + conversation.getConversationID());
                    }
                }
            }
        }
    }

    /**
     * Thread for handling the failing of a conversation.
     */
    private static class FailingConversation implements Runnable {
        /**
         * The conversation to fail.
         */
        private final Conversation conversation;
        /**
         * The message telling the reason for the conversation to fail.
         */
        private final String message;

        /**
         * @param conversation The conversation to fail.
         * @param message      The reason for the conversation to fail.
         */
        FailingConversation(Conversation conversation, String message) {
            this.conversation = conversation;
            this.message = message;
        }

        @Override
        public void run() {
            OperationFailedEvent failedEvent = new OperationFailedEvent(null, message, Collections.<ContributorEvent>emptyList());
            failedEvent.setConversationID(conversation.getConversationID());
            conversation.failConversation(failedEvent);
        }
    }
}
