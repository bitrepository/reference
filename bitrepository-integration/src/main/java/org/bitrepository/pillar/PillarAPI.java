/*
 * #%L
 * Bitmagasin integrationstest
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
package org.bitrepository.pillar;

import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileRequest;

/**
 * Interface for a reference pillar implementation.
 */
interface PillarAPI {
    /**
     * Method for handling a IdentifyPillarsForGetFileRequest.
     * 
     * @param msg The IdentifyPillarsForGetFileRequest to be handled.
     */
    void identifyForGetFile(IdentifyPillarsForGetFileRequest msg);
    
    /**
     * Method for handling a IdentifyPillarsForGetFileIDsRequest.
     * 
     * @param msg The IdentifyPillarsForGetFileIDsRequest to be handled.
     */
    void identifyForGetFileIds(IdentifyPillarsForGetFileIDsRequest msg);

    /**
     * Method for handling a IdentifyPillarsForGetChecksumRequest.
     * 
     * @param msg The IdentifyPillarsForGetChecksumRequest to be handled.
     */
    void identifyForGetChecksum(IdentifyPillarsForGetChecksumsRequest msg);
    
    /**
     * Method for handling a IdentifyPillarsForPutFileRequest.
     * 
     * @param msg The IdentifyPillarsForPutFileRequest to be handled.
     */
    void identifyForPutFile(IdentifyPillarsForPutFileRequest msg);
    
    /**
     * Method for handling a GetChecksumsRequest.
     * 
     * @param msg The GetChecksumsRequest to be handled.
     */
    void getChecksum(GetChecksumsRequest msg);
    
    /**
     * Method for handling a GetFileRequest.
     * 
     * @param msg The GetFileRequest to be handled.
     */
    void getFile(GetFileRequest msg);
    
    /**
     * Method for handling a GetFileIDsRequest.
     * 
     * @param msg The GetFileIDsRequest to be handled.
     */
    void getFileIds(GetFileIDsRequest msg);
    
    /**
     * Method for handling a PutFileRequest.
     * 
     * @param msg The PutFileRequest to be handled.
     */
    void putFile(PutFileRequest msg);
}
