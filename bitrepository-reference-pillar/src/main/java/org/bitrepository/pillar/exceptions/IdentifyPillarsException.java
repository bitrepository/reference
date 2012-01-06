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
 * Exception which wraps bad response information for the identifications. 
 */
@SuppressWarnings("serial")
public class IdentifyPillarsException extends RuntimeException {
    /** The IdentifyResponseInfo wrapped by this exception. Tells the reason for the exception.*/
    private final ResponseInfo identifyResponseInfo;
    
    /**
     * Constructor.
     * @param irInfo The IdentifyResponseInfo for this class to wrap.
     */
    public IdentifyPillarsException(ResponseInfo irInfo) {
        super(irInfo.getResponseText());
        identifyResponseInfo = irInfo;
    }
    
    /**
     * Constructor.
     * @param irInfo The IdentifyResponseInfo for this class to wrap.
     * @param e The exception to wrap into the StackTrace.
     */
    public IdentifyPillarsException(ResponseInfo irInfo, Exception e) {
        super(irInfo.getResponseText(), e);
        identifyResponseInfo = irInfo;
    }
    
    /**
     * @return The wrapped IdentifyResponseInfo.
     */
    public ResponseInfo getResponseInfo() {
        return identifyResponseInfo;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", " + identifyResponseInfo.toString();
    }
}
