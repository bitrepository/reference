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

import org.bitrepository.bitrepositorymessages.GetFileComplete;
import org.bitrepository.bitrepositorymessages.GetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;

/**
 * Internal interface for communication from the GetFileMessageListener.
 * A GetFileClient needs to inherit both this external interface and the internal interface 'GetFileClientAPI'.
 */
public abstract class GetFileClientAPI {
    /**
     * Method for handling a IdentifyPillarsForGetFileReply message.
     * 
     * @param msg The IdentifyPillarsForGetFileReply message to be handled.
     */
    abstract void handleIdentifyPillarsForGetFileResponse(IdentifyPillarsForGetFileResponse msg);
    
    /**
     * Method for handling a GetFileResponse message.
     * 
     * @param msg The GetFileResponse message to be handled.
     */
    abstract void handleGetFileResponse(GetFileResponse msg);
    
    /**
     * Method for handling a GetFileComplete message.
     * 
     * @param msg The GetFileComplete message to be handled.
     */
    abstract void handleGetFileComplete(GetFileComplete msg);
}
