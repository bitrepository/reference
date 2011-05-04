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
package org.bitrepository.access;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;

import java.io.File;
import java.util.List;

/**
 * Interface for GetFileIDs client.
 */
public interface GetFileIDsClient {
    /**
     * Identify potential pillars for retrieving File IDs from pillar.
     *
     * @param slaID The ID of a collection.
     * @return A list of replies from pillars that could respond to this request.
     *         The replies contain pillarID, reply-to-queue and possibly response-times.
     */
    List<IdentifyPillarsForGetFileIDsResponse> identifyPillarsForGetFileIDs(String slaID);

    /**
     * Retrieve a list of File IDs from pillar.
     *
     * @param slaID The ID of a collection.
     * @param queue The queue where the request should be send
     * @param pillarID The ID of the pillar
     * @return A file containing a list of File IDs. The file is in XML format.
     */
    File getFileIDs(String slaID, String queue, String pillarID);

}
