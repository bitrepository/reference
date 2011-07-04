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

import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;

/**
 * Interface for sending messages.
 */
public interface MessageSender {
    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(Alarm content);
    
    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(GetChecksumsFinalResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(GetChecksumsRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(GetChecksumsProgressResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(GetFileFinalResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(GetFileIDsFinalResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(GetFileIDsRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(GetFileIDsProgressResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(GetFileRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(GetFileProgressResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(GetStatusRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(GetStatusProgressResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(GetStatusFinalResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(IdentifyPillarsForGetChecksumsResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(IdentifyPillarsForGetChecksumsRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(IdentifyPillarsForGetFileIDsRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(IdentifyPillarsForGetFileIDsResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(IdentifyPillarsForGetFileRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(IdentifyPillarsForGetFileResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(IdentifyPillarsForPutFileResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(IdentifyPillarsForPutFileRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(PutFileFinalResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(PutFileRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param content       The content of the message.
     */
    void sendMessage(PutFileProgressResponse content);
}
