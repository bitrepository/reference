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
package org.bitrepository.modify;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for keeping track of which files are outstanding at which pillars.
 * It is currently only being handled in the memory. 
 * TODO perhaps use a more permanent storage format for keeping track of these outstanding files, like a file or 
 * a database?
 */
public class OutstandingPutFiles {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /** The map for keeping track of the outstanding files and pillars.
     * Maps between a fileId and a list of pillarIds.*/
    private Map<String, List<String>> outstandingPutFiles 
            = Collections.synchronizedMap(new HashMap<String, 
                    List<String>>());
    
    /** Default constructor.*/
    public OutstandingPutFiles() { }
    
    /**
     * Method for inserting a entry for a file being put to a specific 
     * pillar.
     * 
     * @param fileId The id of the file to be put.
     * @param pillarId The id of the pillar, where the file is being put.
     */
    public void insertEntry(String fileId, String pillarId) {
        // TODO validate arguments?
        
        // Retrieve the list of pillars outstanding for this file. If the
        // the file is not yet outstanding, then create a new list for the 
        // file.
        List<String> pillarIds;
        if(outstandingPutFiles.containsKey(fileId)) {
            pillarIds = outstandingPutFiles.get(fileId);
            if(pillarIds.contains(pillarId)) {
                // TODO handle the scenario, when it is already known, that
                // a pillar is missing this file.
                log.warn("The pillar '" + pillarId + "' is already missing "
                        + " the file '" + fileId + "'");
                return;
            }
        } else {
            pillarIds = new ArrayList<String>();
        }
        
        // put the pillar on the list, and insert it into the outstanding.
        pillarIds.add(pillarId);
        outstandingPutFiles.put(fileId, pillarIds);
    }
    
    /**
     * Method for telling that a file is no longer outstanding at a given
     * pillar. 
     * 
     * @param fileId The file which is no longer outstanding at the given
     * pillar.
     * @param pillarId The pillar where the file is no longer outstanding.
     */
    public void removeEntry(String fileId, String pillarId) {
        // TODO validate arguments?
        
        if(!outstandingPutFiles.containsKey(fileId)) {
            // TODO handle this. Perhaps throw exception?
            log.error("The file '" + fileId + "' is not known to be "
                    + "outstanding anywhere. Not even at '" + pillarId 
                    + "' where it is set to no longer being outstanding.");
            return;
        }
        
        List<String> pillarIds = outstandingPutFiles.remove(fileId);
        if(!pillarIds.contains(pillarId)) {
            // TODO handle this. Perhaps throw exception?
            log.error("The file '" + fileId + "' is known to be "
                    + "outstanding, but not at '" + pillarId + "' where it "
                    + "is set to be removed from the outstanding.");
        } else {
            // remove the pillar
            pillarIds.remove(pillarId);
            log.debug("The file '" + fileId + "' is no longer considered "
                    + "outstanding at pillar '" + pillarId + "'.");
        }
        
        // reinsert the list of outstanding pillars, unless not more pillars
        // are outstanding.
        if(!pillarIds.isEmpty()) {
            outstandingPutFiles.put(fileId, pillarIds);
            log.debug("The file '" + fileId + "' is still outstanding at '"
                    + pillarIds + "'");
        } else {
            log.debug("The file '" + fileId + "' is no longer outstanding "
                    + "at any pillar, and the put is therefore completed.");
        }
    }
    
    /**
     * Method for telling whether a given file is outstanding anywhere.
     * 
     * @param fileId The id of the file, which might be outstanding.
     * @return Whether the file is actually outstanding.
     */
    public boolean isOutstanding(String fileId) {
        // TODO validate argument?
        return outstandingPutFiles.containsKey(fileId);
    }
    
    /**
     * Method for telling whether a given file is outstanding at a specific
     * pillar. 
     * 
     * @param fileId The id of the file, which might be outstanding at the 
     * pillar.
     * @param pillarId The id of the pillar, where the file might be 
     * outstanding.
     * @return Whether the file is outstanding at the given pillar.
     */
    public boolean isOutstandingAtPillar(String fileId, String pillarId) {
        // TODO validate arguments?
        if(!outstandingPutFiles.containsKey(fileId)) {
            return false;
        }
        return outstandingPutFiles.get(fileId).contains(pillarId);
    }
}
