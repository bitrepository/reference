/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.protocol.conversation;

import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.protocol.messagebus.AbstractMessageListener;

/**
 * Implements the generic conversation state functionality, 
 * like timeouts and the definition of the common state attributes.
 */
public abstract class AbstractConversationState extends AbstractMessageListener implements ConversationState {
    /** Handles the mediation of information regarding conversation updates */
    protected final ConversationEventMonitor monitor;
    /** The timer used for timeout checks. */
    private final Timer timer = new Timer();
    /** The timer task for timeout of identify in this conversation. */
    private final TimerTask stateTimeoutTask = new StateTimerTask();
    /** See constructor. */
    private final long stateTimeout;
    
    /** 
     * The constructor for the indicated conversation.
     * @param monitor The monitor to use for notifications
     * @param stateTimeout
     */
    public AbstractConversationState(ConversationEventMonitor monitor, long stateTimeout) {
        this.monitor = monitor;
        this.stateTimeout = stateTimeout;
    }
    
    @Override
    public final void start() {
        timer.schedule(stateTimeoutTask, stateTimeout);
        startState();
    }
    
    /**
     * Handling the end of the state.
     * @ Returns the next state. The new state will automatically have been started.
     */
    public final ConversationState end() {
        return endState();
    }
    
    // Remove when all conversation as switched to use the FinishedState.
    @Override
    public boolean hasEnded() {
        return false;
    }

    /**
     * The timer task class for the outstanding identify requests. When the time is reached the selected pillar should
     * be called requested for the delivery of the file.
     */
    private class StateTimerTask extends TimerTask {
        @Override
        public void run() {
            handleStateTimeout();
        }
    }

    /**
     * Implement by concrete states for handling the start of the state.
     */
    protected abstract void startState();
    /**
     * Implement by concrete states for handling the end of the state.
     * @ Returns the next state. The new state will automatically have been started.
     */
    protected abstract ConversationState endState();
    /**
     * Implement by concrete states for handling timeout for the state.
     */
    protected abstract void handleStateTimeout();
}
