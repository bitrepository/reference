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
 * Indicates and operation has failed to complete
 */
public class OperationFailedEvent extends AbstractOperationEvent {
    /** @see #getInfo() */
    private final String info;
    /** @see #getType() */
    private final static OperationEventType type = OperationEventType.FAILED;
    private final Exception exception;

    /**
     * Constructor with exception information
     * @param info See {@link #getInfo()}
     * @param exception See {@link #getException()} ()}
     */
    public OperationFailedEvent(String info, String conversationID, Exception exception) {
        super(type, info, conversationID);
        this.info = info;
        this.exception = exception;
    }

    /**
     * Plain info constructor.
     * @param info Message describing the failure.
     */
    public OperationFailedEvent(String info, String conversationID) {
        super(type, info, conversationID);
        this.info = info;
        this.exception = null;
    }

    /** Returns the exception causing this failure, if any. Might be null if the failure wasn't caused by an 
     * exception */
    public Exception getException() {
        return exception;
    }

    @Override
    public String additionalInfo() {
        if(exception != null) {
            return exception.getMessage();
        } else {
            return "";
        }
    }
}
