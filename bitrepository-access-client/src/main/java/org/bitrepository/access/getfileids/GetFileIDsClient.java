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
import org.bitrepository.access.ContributorQuery;
import org.bitrepository.client.eventhandler.EventHandler;

/**
 * Interface for GetFileIDs client exposing the bit repository org.bitrepository.org.bitrepository.protocol GetFileIDs functionality.
 */
public interface GetFileIDsClient {
    
    /**
     * Method for requesting a given list of FileIDs from the pillars. Thus requesting validation of the existence of
     * the files at the given pillars.
     * <br/>
     * If the number of fileIDs in a collection is large (10.000's) the fileIDs should  be retrieved in chunks by
     * using the <code>ContributorQuery</code> functionality.
     * <br/>
     * The FileIDs can be requested either through a URL or through the message (give URL = null as argument).
     * <br/>
     * Since every pillar cannot upload their fileids to the same URL, it is extended with the pillarId for the given
     * pillar, e.g.: 'http://upload.url/mypath' + '-pillarId'.
     * <br/>
     * The results are returned through as an special event through the eventHandler, the FileIDsCompletePillarCompete. 
     *
     * @param collectionID Identifies the collection the fileIDs should be retrived from.
     * @param contributorQueries Defines which fileIDs to retrieve. If null all fileIDs from all contributers are
     *                           returned. Note that
     * @param fileID The optional fileID to retrieve file information for. If <code>null</code> file information are
     *               retrieved for all files.
     * @param addressForResult The address for delivering the results of the operation. If this is null, then it is 
     * returned through the messages.
     * @param eventHandler The eventHandler to keep track of the operation.
     */
    public void getFileIDs(
            String collectionID,
            ContributorQuery[] contributorQueries,
            String fileID,
            URL addressForResult,
            EventHandler eventHandler);
}
