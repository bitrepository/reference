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

import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.NoPillarFoundException;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.exceptions.OperationTimeOutException;

/**
 * The <code>GetFileClient</code> is used as a handle for the Bit Repository getFile operation. 
 * 
 */
public interface GetFileClient {

    /**
     * Method for retrieving a file from the pillar able to deliver the file fastest. 
     * 
     * The method will return as soon as the communication has been setup.
     *
     * @param fileId The id of the file to retrieve.
     * @param uploadUrl The url the pillar should upload the file to.
     * @param eventHandler The handler which should receive notifications of the progress events. 
     */
    void getFileFromFastestPillar(String fileId, URL uploadUrl, EventHandler eventHandler);

    /**
     * Method for retrieving a file from the pillar able to deliver the file fastest. 
     * 
     * The method will block until the file has been retrieved.
     *
     * @param fileId The id of the file to retrieve.
     * @param uploadUrl The url the pillar should upload the file to.
     * 
     * @throws NoPillarFoundException The identify request didn't cause any relevant pillar to respond.  
     * @throws OperationTimeOutException The get request timeout.  
     * @throws OperationFailedException The operation failed.
     */
    void getFileFromFastestPillar(String fileId, URL uploadUrl) 
    throws NoPillarFoundException, OperationTimeOutException, OperationFailedException;

    /**
     * Method for retrieving a file from a specific pillar. 
     * 
     * The method will return as soon as the communication has been setup.
     * @param pillarId The id of pillar, where the file should be retrieved from.
     *
     * @param fileId The id of the file to retrieve.
     * @param uploadUrl The url the pillar should upload the file to.
     * @param eventHandler The handler which should receive notifications of the progress events. 
     */
    void getFileFromSpecificPillar(String fileId, URL uploadUrl, String pillarId, EventHandler eventHandler);

    /**
     * Method for retrieving a file from a specific pillar.
     *
     * @param fileId The id of the file to retrieve.
     * @param uploadUrl The url the pillar should upload the file to.
     * @param pillarId The id of pillar, where the file should be retrieved from.
     * @throws NoPillarFoundException The identify request didn't cause any relevant pillar to respond.  
     * @throws OperationTimeOutException The get request timeout.  
     * @throws OperationFailedException The operation failed.
     */
    void getFileFromSpecificPillar(String fileId, URL uploadUrl, String pillarId)
    throws NoPillarFoundException, OperationTimeOutException, OperationFailedException;
}
