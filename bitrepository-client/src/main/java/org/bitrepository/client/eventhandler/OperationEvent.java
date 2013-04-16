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
 * Container for information regarding events occurring during an operation on the Bit Repository.
 */
public interface OperationEvent {
    /**
     * Defines the different types of events that can be received. These are:<ol>
     * <li>PillarIdentified: An identify response has been received from a pillar.
     * <li>PillarSelected: Enough responses has now been received from pillar to select a pillar to perform the 
     * operation on.
     * <li>RequestSent: A request for the operation has ben sent to the relevant pillar(s). 
     * <li>Progress: In case of longer operations (e.g. requiring file transfers) progress information might be 
     * received from the pillars.
     * <li>PartiallyComplete: The cases when part of the operation has been completed (e.g. when the operation has been
     * to be performed at several pillars, and it has been completed on one of them).
     * <li>Complete: The operation has finished
     * </ol>
     * The following error types exist:<ol>
     * <li>Failed: A general failure occurred during the operation
     * <li>NoPillarFound: No relevant response was received before a timeout occurred.
     * <li>TimeOut: The operation did't finish before a timeout occurred.
     * </ol>
     */
    public enum OperationEventType {
        IDENTIFY_REQUEST_SENT,
        COMPONENT_IDENTIFIED,
        IDENTIFICATION_COMPLETE,
        REQUEST_SENT,
        PROGRESS,
        COMPONENT_COMPLETE,
        COMPLETE, 
        COMPONENT_FAILED,
        FAILED,
        IDENTIFY_TIMEOUT, 
        WARNING 
    }
    
    /**
     * A string representation of what has happened
     * @return
     */
    String getInfo();
    
    /**
     * Used to get the type of event.
     * @return A <code>OperationEventType</code> categorizing this event.
     */
    OperationEventType getEventType();

    /**
     * Used to get the type of operation.
     * @return A <code>OperationEventType</code> categorizing this event.
     */
    OperationType getOperationType();

    /**
     * Used to get the fileID this operation is performed on, if any.
     * @return A <code>OperationEventType</code> categorizing this event.
     */
    String getFileID();

    /**
     * Deliver the conversation ID of the event.
     */
    String getConversationID();
    
    /**
     * The ID of the collection that the  
     */
    String getCollectionID();
}
