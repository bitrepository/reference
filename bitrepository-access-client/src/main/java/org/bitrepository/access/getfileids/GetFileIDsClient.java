/*
 * #%L
 * Bitrepository Access Client
 * *
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
package org.bitrepository.access.getfileids;

import java.net.URL;
import java.util.Collection;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.OperationFailedException;

/**
 * Interface for GetFileIDs client exposing the bit repository protocol GetFileIDs functionality.
 */
public interface GetFileIDsClient {
    
    /**
     * Method for requesting a given list of FileIDs from pillars. Thus requesting validation of the existence of 
     * the files at the given pillars.
     * <br/>
     * The FileIDs can be requested either through a URL or through the message (give URL = null as argument).
     * <br/>
     * Since every pillar cannot upload their fileids to the same URL, it is extended with the pillarId for the given
     * pillar, e.g.: 'http://upload.url/mypath' + '-pillarId'.
     * <br/>
     * The results are returned through as an special event through the eventHandler, the FileIDsCompletePillarCompete. 
     *  
     * @param pillarIDs The list of pillars which should be requested for the FileIDs.
     * @param fileIDs The ids for the requested files. 
     * @param addressForResult The address for delivering the results of the operation. If this is null, then it is 
     * returned through the messages.
     * @param eventHandler The eventHandler to keep track of the operation. 
     * @param auditTrailInformation The auditTrailInformation for the requests.
     */
    public void getFileIDs(Collection<String> pillarIDs, FileIDs fileIDs, URL addressForResult, EventHandler eventHandler, 
            String auditTrailInformation) throws OperationFailedException;
    
    /**
     * Method to perform a graceful shutdown of the client.
     */
    void shutdown();
}
