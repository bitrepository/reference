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
package org.bitrepository.service.exception;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;

/**
 * Exception which wraps bad response information for the validation of the operation requests. 
 */
@SuppressWarnings("serial")
public class InvalidMessageException extends RequestHandlerException {
    /**
     * Constructor.
     * @param rCode The response code.
     * @param rText The text for the response info.
     * @param collectionID The id of the collection.
     */
    public InvalidMessageException(ResponseCode rCode, String rText, String collectionID) {
        super(rCode, rText, collectionID);
    }
    
    /**
     * Constructor.
     * @param rCode The response code.
     * @param rText The text for the response info.
     * @param collectionID The id of the collection.
     * @param e The exception to wrap into the StackTrace.
     */
    public InvalidMessageException(ResponseCode rCode, String rText, String collectionID, Exception e) {
        super(rCode, rText, collectionID, e);
    }
    
    /**
     * Constructor.
     * @param rInfo The response info.
     * @param collectionID The id of the collection.
     */
    public InvalidMessageException(ResponseInfo rInfo, String collectionID) {
        super(rInfo, collectionID);
    }
}
