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
import org.bitrepository.bitrepositorymessages.PutFileComplete;
import org.bitrepository.bitrepositorymessages.PutFileResponse;

/**
 * This is the internal interface for the PutClientMessage listener.
 * A put client must have both the internal (PutClientAPI) and the external (PutClientExternalAPI) APIs.
 */
public abstract class PutClientAPI {
    /**
     * Method for handling a PutFileResponse. This message tells how far in the storage process the given pillar is. 
     * It is possible for the pillars have a storage procedure which involves several steps before the file is 
     * properly stored. After each step one of these PutFileResponse messages should be sent, and only when the 
     * storage process is finished should the final PutFileComplete message be sent.
     * 
     * @param msg The PutFileResponse to be handled.
     */
    abstract void handlePutResponse(PutFileResponse msg);
    
    /**
     * Method for handling a PutFileComplete message.
     * 
     * @param msg The PutFileComplete message to be handled.
     */
    abstract void handlePutComplete(PutFileComplete msg);
    
    /**
     * Method for handling the IdentifyPillarsForPutFileReply messages.
     * 
     * @param msg The IdentifyPillarsForPutfileReply message.
     */
    abstract void identifyResponse(IdentifyPillarsForPutFileResponse msg);
}
