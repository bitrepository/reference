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
package org.bitrepository.client.eventhandler;

import org.bitrepository.protocol.OperationType;

/**
 * A general implementation of <code>OperationEvent</code>.
 */
public abstract class AbstractOperationEvent implements OperationEvent {
    private OperationEventType type;
    private OperationType operationType;
    private String fileID;
    private String info;
    private String conversationID;
    private String collectionID;

    @Override 
    public String getCollectionID() {
        return collectionID;
    }
    
    public void setCollectionID(String collectionID) {
        this.collectionID = collectionID;
    }
    
    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public OperationEventType getEventType() {
        return type;
    }

    @Override
    public OperationType getOperationType() {
        return operationType;
    }
    @Override
    public String getFileID() {
        return fileID;
    }

    @Override
    public String getConversationID() {
        return conversationID;
    }

    /** @see #getEventType */
    public void setEventType(OperationEventType type) {
        this.type = type;
    }

    /** @see #getOperationType */
    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    /** @see #getFileID */
    public void setFileID(String fileID) {
        this.fileID = fileID;
    }

    /** @see #getInfo */
    public void setInfo(String info) {
        this.info = info;
    }

    /**
     * @param conversationID See {@link #getConversationID}
     */
    public void setConversationID(String conversationID) {
        this.conversationID = conversationID;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getConversationID() + ": ");
        sb.append(operationType);
        if (fileID != null) {
            sb.append(" for file " + fileID);
        }
        sb.append(": " + getEventType() + ": ");
        if (additionalInfo() != null) {
            sb.append(additionalInfo());
        }
        if (getInfo() != null) {
            sb.append(", " + getInfo());
        }
        return sb.toString();
    }
    
    /**
     * Deliver additional information in a string form. The string returned will be appended to the toString value.
     */
    abstract protected String additionalInfo();
}