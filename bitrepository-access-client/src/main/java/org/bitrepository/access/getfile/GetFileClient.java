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
package org.bitrepository.access.getfile;

import java.net.URL;

/**
 * Interface for the external use of a GetFileClient.
 * A GetFileClient needs to inherit both this external interface and the internal interface 'GetFileClientAPI'.
 */
public interface GetFileClient {
    /**
     * Method for retrieving a file from the pillar able to deliver the file fastest.
     *
     * @param fileId The id of the file to retrieve.
     * @param uploadUrl The url the pillar should upload the file to.
     */
    void getFileFromFastestPillar(String fileId, URL uploadUrl);
    
    /**
     * Method for retrieving a file from a specific pillar.
     *
     * @param fileId The id of the file to retrieve.
     * @param uploadUrl The url the pillar should upload the file to.
     * @param pillarId The id of pillar, where the file should be retrieved from.
     */
    void getFileFromSpecificPillar(String fileId, URL uploadUrl, String pillarId);
    // ToDo This operation shouldn't need a pillarTopicId has it should be retrieved through a IdentifyPillar request
    // prior to the actual getFileRequest.
}
