/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.exceptions;

import org.bitrepository.bitrepositoryelements.ResponseInfo;

/**
 * Exception which wraps bad response information for the validation of the operation requests. 
 */
public class InvalidMessageException extends RuntimeException {
    /** The ResponseInfo wrapped by this exception. Tells the reason for the exception.*/
    private final ResponseInfo responseInfo;
    
    /**
     * Constructor.
     * @param rInfo The ResponseInfo for this class to wrap.
     */
    public InvalidMessageException(ResponseInfo rInfo) {
        super(rInfo.getResponseText());
        responseInfo = rInfo;
    }
    
    /**
     * Constructor.
     * @param rInfo The ResponseInfo for this class to wrap.
     * @param e The exception to wrap into the StackTrace.
     */
    public InvalidMessageException(ResponseInfo rInfo, Exception e) {
        super(rInfo.getResponseText(), e);
        responseInfo = rInfo;
    }
    
    /**
     * @return The wrapped ResponseInfo.
     */
    public ResponseInfo getResponseInfo() {
        return responseInfo;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", " + responseInfo.toString();
    }
}
