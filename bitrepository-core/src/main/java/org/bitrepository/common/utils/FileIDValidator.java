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
package org.bitrepository.common.utils;

import java.util.regex.Pattern;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.common.settings.Settings;

/**
 * Component for validating the id of a file.
 */
public class FileIDValidator {
    /** The regex pattern for the file ids.*/
    protected String regex;
    /** The system limitation for a file id (length 1-254, and no control letters).*/
    private static final String SYSTEM_LIMIT = "[^\\p{Cntrl}]{1,254}";

    /**
     * @param settings The context for the pillar.
     */
    public FileIDValidator(Settings settings) {
        regex = settings.getRepositorySettings().getProtocolSettings().getAllowedFileIDPattern();
        if(regex != null && regex.isEmpty()) {
            regex = null;
        }
    }

    /**
     * Validates the given fileID. Both against the setting and against the system limit.
     * @param fileID The file id to validate.
     * @return invalid description if the id is invalid, else null.
     */
    public ResponseInfo validateFileID(String fileID)  {
        try {
            checkFileID(fileID);
            return null;
        } catch (IllegalArgumentException iae) {
            ResponseInfo ri = new ResponseInfo();
            ri.setResponseCode(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
            ri.setResponseText(iae.getMessage());
            return ri;
        }
    }

    /**
     * @param fileID the file id to check
     * @throws IllegalArgumentException The fileID violated the fileID format constraints.
     */
    public void checkFileID(String fileID) {
        if (fileID != null) {
            if(regex != null) {
                if(!Pattern.matches(regex, fileID)) {
                    throw new IllegalArgumentException("The fileID '" + fileID + "' is invalid against the fileID regex '"
                            + regex + "'.");
                }
                if(!Pattern.matches(SYSTEM_LIMIT, fileID)) {
                    throw new IllegalArgumentException("The fileID '" + fileID + "' is invalid against the system limit regex '"
                            + SYSTEM_LIMIT + "'.");
                }
            }
        }
    }
}
