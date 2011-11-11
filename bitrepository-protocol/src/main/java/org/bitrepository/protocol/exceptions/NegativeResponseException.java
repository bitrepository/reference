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
package org.bitrepository.protocol.exceptions;

import org.bitrepository.bitrepositoryelements.ResponseCode;

/**
 * Used to indicate that an unexpected response has been received.
 */
public class NegativeResponseException extends Exception {
    private final ResponseCode errorcode;

    /**
     * Constructor with both message and cause exception
     * @param Description of this exception
     * @param The errorcode causing the exception.
     */
    public NegativeResponseException(String message, ResponseCode errorcode) {
        super(message);
        this.errorcode = errorcode;
    }

    /**
     * @return The errorcode causing the exception.
     */
    public ResponseCode getErrorcode() {
        return errorcode;
    }  
}
