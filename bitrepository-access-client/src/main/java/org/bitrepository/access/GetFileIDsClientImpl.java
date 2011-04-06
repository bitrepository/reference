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

import java.io.File;
import java.util.List;

/**
 * Prototype GetFileIDs Client
 */
public class GetFileIDsClientImpl implements GetFileIDsClient {
    /**
     * Identify potential pillars for retrieving File IDs from pillar.
     * If sendIdentifyPillarsForGetFileIDsRequest has been invoked and replies
     * received recently, these replies are returned.
     * Otherwise sendIdentifyPillarsForGetFileIDsRequest is invoked and
     * the method waits for replies and returns these.
     * The waiting time is limited to a set time out.
     *
     * @param slaID The ID of a collection.
     * @return A list of IDs of pillars that could respond to this request.
     */
    @Override
    public List<String> identifyPillarsForGetFileIDs(String slaID) {
        return null;  //Todo implement identifyPillarsForGetFileIDs
    }

    @Override
    public void sendIdentifyPillarsForGetFileIDsRequest(String slaID) {
        //Todo implement sendIdentifyPillarsForGetFileIDsRequest
    }

    @Override
    public File getFileIDs(String slaID, String pillarID) {
        return null;  //Todo implement getFileIDs
    }

    @Override
    public void sendGetFileIDsRequest(String slaID, String pillarID) {
        //Todo implement sendGetFileIDsRequest
    }
}
