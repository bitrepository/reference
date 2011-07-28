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

import org.bitrepository.protocol.exceptions.OperationFailedException;

/**
 * Indicates and operation has failed to complete
 */
public class OperationFailedEvent implements OperationEvent<OperationFailedException> {
    /** @see #getInfo() */
    private final String info;
    /** @see #getType() */
    private final OperationEventType type = OperationEventType.Failed;
    /** @see #getState() */
    private final OperationFailedException exception;

    /**
     * Constructor with exception information
     * @param info
     * @param exception
     */
    public OperationFailedEvent(String info, OperationFailedException exception) {
        super();
        this.info = info;
        this.exception = exception;
    }

    /**
     * Plain info constructor.
     * @param info Message describing the failure.
     */
    public OperationFailedEvent(String info) {
        super();
        this.info = info;
        this.exception = null;
    }

    @Override
    public String getInfo() {
        return info;
    }

    @Override
    public OperationEventType getType() {
        return type;
    }

    /** Returns the exception causing this failure, if any. Might be null if the failure wasn't caused by an 
     * exception */
    @Override
    public OperationFailedException getState() {
        return exception;
    }
}
