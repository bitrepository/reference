/*
 * #%L
 * bitrepository-access-client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Container for the data involving a specific fileId:
 * The id of the file and the id of the SLA.
 */
public class FileIdInstance {
    /** The map between the unique identification and the corresponding FileIdInstances.*/
    private static Map<String, FileIdInstance> ids = Collections.synchronizedMap(new HashMap<String, FileIdInstance>());
    
    /**
     * Method for retrieving a FileIdInstance. 
     * Ensures that only one FileIdInstace is created for a unique combination of fileId and slaId.
     * 
     * TODO There is a risk if a file id or sla id contains "##".
     * 
     * @param fileId The id of the file.
     * @param slaId The id of the SLA, where the file belongs.
     * @return The FileIdInstance corresponding to the fileId and the slaId.
     */
    public static FileIdInstance getInstance(String fileId, String slaId) {
        String uniqueId = getUniqueID(fileId, slaId);
        
        // If it does not already exist, then a new will be created and inserted into the map.
        if(!ids.containsKey(uniqueId)) {
            ids.put(uniqueId, new FileIdInstance(fileId, slaId));
        }
        return ids.get(uniqueId);
    }
    
    /** 
     * Retrieves the unique combination of the fileId and the slaId.
     * @param fileId The id of the file.
     * @param slaId The id of the sla, where the file belongs.
     * @return The values combined into a unique string for the combination.  
     */
    public static String getUniqueID(String fileId, String slaId) {
        return slaId + "##" + fileId;
    }

    /** The id of the file.*/
    private final String fileId;
    /** The id of the SLA, where the file belongs.*/
    private final String slaId;
    
    /**
     * Constructor. 
     * @param fileId The id of the file.
     * @param slaId The id of the SLA, where the file belongs.
     */
    private FileIdInstance(String fileId, String slaId) {
        this.fileId = fileId;
        this.slaId = slaId;
    }
    
    /**
     * Retrieval of the id of the file.
     * @return The id of the file.
     */
    public String getFileId() {
        return fileId;
    }
    
    /**
     * Retrieval of the id of the SLA, where the file belongs.
     * @return The id of the SLA.
     */
    public String getSlaId() {
        return slaId;
    }
    
    @Override
    public String toString() {
        return "File id '" + fileId + "', SLA id '" + slaId + "'.";
    }
}
