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
package org.bitrepository.protocol.conversation;

import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.eventhandler.OperationFailedEvent;
import org.bitrepository.protocol.messagebus.AbstractMessageListener;
import org.bitrepository.protocol.messagebus.MessageSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract super class for conversations. This super class will handle sending all messages with the correct
 * conversation id, and simply log messages received. Overriding implementations should override the behaviour for
 * receiving specific messages.
 *
 * @param <T> The result of this conversation.
 */
public abstract class AbstractConversation extends AbstractMessageListener implements Conversation {
    /** The message bus used for sending messages. */
    public final MessageSender messageSender;
    /** The conversation ID. */
    private final String conversationID;
    /** @see Conversation#getStartTime() */
    private final long startTime;
    /** The logger for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** Takes care of publishing update information */
    private final ConversationEventMonitor monitor;
    /** Used for storing exceptions generates as result of a message reception, so it can be thrown to the initial 
     * operation initiator */
//    protected OperationFailedException operationFailedException;
    /** Is this conversation a result of a blocking call*/
    protected boolean blocking;
    /** Handles blocks */
    private final FlowController flowController;

    /**
     * Initialize a conversation on the given message bus.
     *
     * @param messagebus The message bus used for exchanging messages.
     * @param conversationID The conversation ID for this conversation.
     */
    public AbstractConversation(
            MessageSender messageSender, 
            String conversationID, 
            EventHandler eventHandler, 
            FlowController flowController) {
        this.messageSender = messageSender;
        this.conversationID = conversationID;
        this.startTime = System.currentTimeMillis();
        this.monitor = new ConversationEventMonitor(this, eventHandler);
        this.flowController = flowController;
        flowController.setConversation(this);
    }

    @Override
    public String getConversationID() {
        return conversationID;
    }
    
    @Override
    public long getStartTime() {
        return startTime;
    }

    /** 
     * Will start the conversation and either:<ol>
     * <li> If no event handler has been defined the method will block until the conversation has finished.</li>
     * <li> If a event handler has been defined the method will return after the conversation is started.</li>
     * </ol>
     * @return 
     */
    @Override
    public void startConversation() {
        getConversationState().start();  
    }
    
    /**
     * Use for failing this conversation
     * @param info A description of the cause.
     * @param e The causing exception.
     */
    public synchronized void failConversation(String info, Exception e) {
        failConversation(new OperationFailedEvent(info, e));
    }

    /**
     * Use for failing this conversation
     * @param info A description of the cause.
     */
    public synchronized void failConversation(String info) {
        failConversation(new OperationFailedEvent(info));
    }

    @Override
    public synchronized void failConversation(OperationFailedEvent failedEvent) {
        monitor.operationFailed(failedEvent);
        endConversation();
        flowController.unblock();
    }

    /**
     * @return The monitor for distributing update information
     */
    public ConversationEventMonitor getMonitor() {
        return monitor;
    }
    
    /**
     * @return The controller with the responsibility of handling blocking
     */
    public FlowController getFlowController() {
        return flowController;
    }
    
    /**
     * @return The state of this conversation.
     */
    public abstract ConversationState getConversationState();
}
