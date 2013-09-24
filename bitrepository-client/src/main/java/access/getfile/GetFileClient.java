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
package access.getfile;

import java.net.URL;

import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.client.BitrepositoryClient;
import org.bitrepository.client.eventhandler.EventHandler;

/**
 * The <code>GetFileClient</code> is used as a handle for the Bit Repository getFile operation. 
 *
 */
public interface GetFileClient extends BitrepositoryClient {

    /**
     * Method for retrieving a file from the pillar able to deliver the file fastest. 
     *
     * The method will return as soon as the communication has been setup.
     *
     * @param  collectionID Identifies the collection the file should be retrieved from.
     * @param fileId The id of the file to retrieve.
     * @param filePart The part of the file, which is wanted. If null, then the whole file is retrieved.
     * @param uploadUrl The url the pillar should upload the file to.
     * @param eventHandler The handler which should receive notifications of the progress events.
     * @param auditTrailInformation Additional information to add to the audit trail created because of this operation.
     */
    void getFileFromFastestPillar(String collectionID, String fileId, FilePart filePart, URL uploadUrl,
                                  EventHandler eventHandler,
                                  String auditTrailInformation);

    /**
     * Method for retrieving a file from a specific pillar. 
     *
     * The method will return as soon as the communication has been setup.
     * @param  collectionID Identifies the collection the file should be retrieved from.
     * @param fileId The id of the file to retrieve.
     * @param filePart The part of the file, which is wanted. If null, then the whole file is retrieved.
     * @param uploadUrl The url the pillar should upload the file to.
     * @param pillarId The id of pillar, where the file should be retrieved from.
     * @param eventHandler The handler which should receive notifications of the events occurring in connection with 
     * the pillar communication.
     * @param auditTrailInformation Additional information to add to the audit trail created because of this operation.
     */
    void getFileFromSpecificPillar(String collectionID, String fileId, FilePart filePart, URL uploadUrl,
                                   String pillarId,
                                   EventHandler eventHandler, String auditTrailInformation);
}
