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
package org.bitrepository.protocol;

import org.bitrepository.bitrepositorymessages.GetChecksumsComplete;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.GetFileComplete;
import org.bitrepository.bitrepositorymessages.GetFileIDsComplete;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileComplete;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileResponse;

/**
 * Interface for sending messages.
 *
 * TODO: Recipient queue should be extracted from message, rather than be given as parameter
 */
public interface MessageSender {
    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, GetChecksumsComplete content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, GetChecksumsRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, GetChecksumsResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, GetFileComplete content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, GetFileIDsComplete content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, GetFileIDsRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, GetFileIDsResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, GetFileRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, GetFileResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, IdentifyPillarsForGetChecksumsResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, IdentifyPillarsForGetChecksumsRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, IdentifyPillarsForGetFileIDsRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, IdentifyPillarsForGetFileIDsResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, IdentifyPillarsForGetFileRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, IdentifyPillarsForGetFileResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, IdentifyPillarsForPutFileResponse content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, IdentifyPillarsForPutFileRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, PutFileComplete content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, PutFileRequest content);

    /**
     * Method for sending a message on a specific destination.
     *
     * @param destinationId The id for the destination to send message.
     * @param content       The content of the message.
     */
    void sendMessage(String destinationId, PutFileResponse content);
}
