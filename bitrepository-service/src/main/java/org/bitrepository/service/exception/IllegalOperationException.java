/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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

/**
 * Exception for telling, that a given operation is illegal.
 * This might involve: deleting with a invalid checksum, performing the 'Get' operation on a ChecksumPillar, etc.
 */
@SuppressWarnings("serial")
public class IllegalOperationException extends RequestHandlerException {
    private String fileID;
    
    /**
     * Constructor.
     * @param rCode The response code.
     * @param rText The text for the response info.
     * @param collectionID The id of the collection. Use 'null' if no collection is relevant.
     * @param fileID The id of the file regarding the illegal operation. Use null, if no file.
     */
    public IllegalOperationException(ResponseCode rCode, String rText, String collectionID, String fileID) {
        super(rCode, rText, collectionID);
        this.fileID = fileID;
    }
    
    /**
     * Constructor.
     * @param rCode The response code.
     * @param rText The text for the response info.
     * @param collectionID The id of the collection. Use 'null' if no collection is relevant.
     * @param fileID The id of the file regarding the illegal operation. Use null, if no file.
     * @param e The exception to wrap into the StackTrace.
     */
    public IllegalOperationException(ResponseCode rCode, String rText, String collectionID, String fileID, Exception e) {
        super(rCode, rText, collectionID, e);
        this.fileID = fileID;
    }
    
    /**
     * @return fileID field.
     */
    public String getFileId() {
        return fileID;
    }
}
