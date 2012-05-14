/*
 * #%L
 * Bitrepository Reference Pillar
 * *
 * $Id: PutFileRequestHandler.java 687 2012-01-09 12:56:47Z ktc $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/main/java/org/bitrepository/pillar/messagehandler/PutFileRequestHandler.java $
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
package org.bitrepository.pillar.common;

import java.util.regex.Pattern;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.service.exception.InvalidMessageException;

/**
 * Component for validating the id of a file.
 */
public class FileIDValidator {
    /** The regex pattern for the file ids.*/
    private String regex;
    
    /**
     * Constructor.
     * @param context The context for the pillar.
     */
    public FileIDValidator(PillarContext context) {
        regex = context.getSettings().getCollectionSettings().getProtocolSettings().getAllowedFileIDPattern();
        if(regex != null && regex.isEmpty()) {
            regex = null;
        }
    }
    
    /**
     * Validates the given fileID.
     * @param fileID The file id to validate.
     * @throws InvalidMessageException If the id is invalid.
     */
    public void validateFileID(String fileID) throws InvalidMessageException {
        if(regex != null && fileID != null) {
            if(!Pattern.matches(regex, fileID)) {
                ResponseInfo ri = new ResponseInfo();
                ri.setResponseCode(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
                ri.setResponseText("The fileID '" + fileID + "' is invalid against the fileID regex '"
                        + regex + "'.");
                throw new InvalidMessageException(ri);
            }
        }
    }
}
