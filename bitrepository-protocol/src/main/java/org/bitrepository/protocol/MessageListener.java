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
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsReply;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsReply;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileReply;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileReply;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileComplete;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileResponse;

/** This interface defines a consumer of messages in the bitrepository
 * protocol. */
public interface MessageListener {
    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetChecksumsComplete message);

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
    void onMessage(GetChecksumsResponse message);

    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetFileComplete message);

    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(GetFileIDsComplete message);

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
    void onMessage(GetFileIDsResponse message);

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
    void onMessage(GetFileResponse message);

    /**
     * Action to perform upon receiving a message.
     *
     * @param message The message received.
     */
    void onMessage(IdentifyPillarsForGetChecksumsReply message);

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
    void onMessage(IdentifyPillarsForGetFileIDsReply message);

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
    void onMessage(IdentifyPillarsForGetFileReply message);

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
    void onMessage(IdentifyPillarsForPutFileReply message);

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
    void onMessage(PutFileComplete message);

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
    void onMessage(PutFileResponse message);
}
