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

    protected AbstractOperationEvent(OperationEventType type, String collectionID, String info) {
        this.type = type;
        this.info = info;
        this.collectionID = collectionID;
    }

    protected AbstractOperationEvent(OperationEventType type, String collectionID) {
        this.type = type;
        this.info = null;
        this.collectionID = collectionID;
    }

    /**
     * @deprecated Use the constructors setting the OperationEventType
     * @see AbstractOperationEvent#AbstractOperationEvent(OperationEventType, String)
     */
    @Deprecated(forRemoval = true)
    public AbstractOperationEvent(String collectionID) {
        this.collectionID = collectionID;
    }

    @Override
    public String getCollectionID() {
        return collectionID;
    }

    /**
     * @deprecated collectionId should be final and set by constructor.
     */
    @Deprecated(forRemoval = true)
    public void setCollectionID(final String collectionID) {
        this.collectionID = collectionID;
    }

    @Override
    public String getInfo() {
        return info;
    }


    /**
     * @deprecated info should be final and set by constructor.
     */
    @Deprecated(forRemoval = true)
    public void setInfo(final String info) {
        this.info = info;
    }

    @Override
    public OperationEventType getEventType() {
        return type;
    }


    /**
     * @deprecated eventType should be final and set by constructor.
     */
    @Deprecated(forRemoval = true)
    public void setEventType(final OperationEventType type) {
        this.type = type;
    }

    @Override
    public String getFileID() {
        return fileID;
    }

    /**
     * @param fileID The ID of the file
     * @see #getFileID
     */
    public void setFileID(String fileID) {
        this.fileID = fileID;
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
    public OperationType getOperationType() {
        return operationType;
    }

    /**
     * @param operationType The {@link OperationType} to set as the operation type
     * @see #getOperationType
     */
    public void setOperationType(OperationType operationType) {
        this.operationType = operationType;
    }

    @Override
    public final String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getConversationID()).append(": ");
        sb.append(operationType);
        if (fileID != null) {
            sb.append(" for file ").append(fileID);
        }
        sb.append(": ").append(getEventType()).append(": ");
        if (additionalInfo() != null) {
            sb.append(additionalInfo());
        }
        if (getInfo() != null) {
            sb.append(", ").append(getInfo());
        }
        return sb.toString();
    }

    /**
     * @return The additional information in a string form. The string returned will be appended to the toString value.
     */
    abstract protected String additionalInfo();
}