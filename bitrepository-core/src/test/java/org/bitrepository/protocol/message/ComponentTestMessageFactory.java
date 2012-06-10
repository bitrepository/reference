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
package org.bitrepository.protocol.message;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.CorrelationIDGenerator;

public class ComponentTestMessageFactory extends TestMessageFactory {
    protected final Settings testerSettings;

    public ComponentTestMessageFactory(Settings testerSettings) {
        super(testerSettings.getCollectionID());
        this.testerSettings = testerSettings;
    }

    /**
     * Use for initializing message in a existing conversation.
     */
    public void initializeMessageToComponentUnderTest(Message message, Message messageToReplyTo) {
        initializeMessageToComponent(message);
        message.setCorrelationID(messageToReplyTo.getCorrelationID());
        message.setTo(messageToReplyTo.getReplyTo());
    }

    /**
     * Use for initializing message which begin a conversation. A correlationID is generated, and the call is delegated
     * to the {@link #initializeMessageToComponentUnderTest(Message, Message)}
     * method.
     */
    public void initializeMessageToComponentUnderTest(Message message) {
        initializeMessageToComponent(message);
        message.setCorrelationID(CorrelationIDGenerator.generateConversationID());
        message.setTo(testerSettings.getCollectionDestination());
    }

    private void initializeMessageToComponent(Message message) {
        initializeMessageDetails(message);
        message.setFrom(testerSettings.getComponentID());
        message.setReplyTo(testerSettings.getReceiverDestinationID());
    }
}
