/*
 * #%L
 * Bitmagasin modify client
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

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;

/**
 * This is the internal interface for the PutClientMessage listener.
 * A put client must have both the internal (PutClientAPI) and the external (PutClientExternalAPI) APIs.
 */
public abstract class PutClientAPI {
    /**
     * Method for handling a PutFileProgressResponse. This message tells how far in the storage process the given pillar is. 
     * It is possible for the pillars have a storage procedure which involves several steps before the file is 
     * properly stored. After each step one of these PutFileProgressResponse messages should be sent, and only when the 
     * storage process is finished should the final PutFileFinalResponse message be sent.
     * 
     * @param msg The PutFileProgressResponse to be handled.
     */
    abstract void handlePutProgressResponse(PutFileProgressResponse msg);
    
    /**
     * Method for handling a PutFileFinalResponse message.
     * 
     * @param msg The PutFileFinalResponse message to be handled.
     */
    abstract void handlePutFinalResponse(PutFileFinalResponse msg);
    
    /**
     * Method for handling the IdentifyPillarsForPutFileResponse messages.
     * 
     * @param msg The IdentifyPillarsForPutfileResponse message.
     */
    abstract void identifyResponse(IdentifyPillarsForPutFileResponse msg);
}
