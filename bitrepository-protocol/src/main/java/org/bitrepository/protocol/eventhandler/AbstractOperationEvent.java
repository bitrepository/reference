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
package org.bitrepository.protocol.eventhandler;

/**
 * A general implementation of <code>OperationEvent</code>.
 */
public class AbstractOperationEvent implements OperationEvent {
    private final OperationEventType type;
    private final String info;
    private String conversationID;

    /**
     * @param type See {@link #getType()}
     * @param info See {@link #getInfo()}
     * @param conversationID See {@link #getConversationID}
     */
    public AbstractOperationEvent(OperationEventType type, String info, String conversationID) {
        this.type = type;
        this.info = info;
        this.conversationID = conversationID;
    }

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public OperationEventType getType() {
        return type;
    }

    @Override
    public String getConversationID() {
        return conversationID;
    }

    /**
     * @param conversationID See {@link #getConversationID}
     */
    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    @Override
    public final String toString() {
        return getType() + ": " + "ID: " + getConversationID() + ", " + additionalInfo() + ", " + getInfo();
    }
    
    /**
     * Deliver additional information in a string form. The string returned will be appended to the toString value.
     */
    protected String additionalInfo() {
        return "";
    }
}