/*
 * #%L
 * Bitrepository Protocol
 * 
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
package org.bitrepository.protocol.messagebus;

import org.bitrepository.bitrepositorymessages.*;


/** This interface defines a consumer of messages in the bitrepository
 * org.bitrepository.org.bitrepository.protocol.
 *
 * If an implementation does not support a method, it may throw {@link UnsupportedOperationException}
 * @deprecated Use the generic {@link MessageListener} instead.
 */
public interface SpecificMessageListener extends MessageListener {
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(AlarmMessage message);

    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(DeleteFileFinalResponse message);

    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(DeleteFileProgressResponse message);

    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(DeleteFileRequest message);

    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetAuditTrailsRequest message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetAuditTrailsProgressResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetAuditTrailsFinalResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetChecksumsFinalResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetChecksumsRequest message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetChecksumsProgressResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetFileFinalResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetFileIDsFinalResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetFileIDsRequest message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetFileIDsProgressResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetFileRequest message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetFileProgressResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetStatusRequest message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyPillarsForDeleteFileRequest message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyPillarsForDeleteFileResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyPillarsForGetChecksumsResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyPillarsForGetChecksumsRequest message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyPillarsForGetFileIDsResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyPillarsForGetFileIDsRequest message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyPillarsForGetFileResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyPillarsForGetFileRequest message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyPillarsForPutFileResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyPillarsForPutFileRequest message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyPillarsForReplaceFileRequest message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyPillarsForReplaceFileResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(PutFileFinalResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(PutFileRequest message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(PutFileProgressResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(ReplaceFileRequest message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(ReplaceFileFinalResponse message);
    
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(ReplaceFileProgressResponse message);

    /**
     * Action to perform upon receiving a general message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyContributorsForGetAuditTrailsRequest message);
    
    /**
     * Action to perform upon receiving a general message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyContributorsForGetAuditTrailsResponse message);
    
    /**
     * Action to perform upon receiving a general message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyContributorsForGetStatusRequest message);

    void onMessage(IdentifyContributorsForGetStatusResponse message);
}
