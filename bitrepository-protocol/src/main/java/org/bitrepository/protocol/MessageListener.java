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

import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;

/** This interface defines a consumer of messages in the bitrepository
 * protocol.
 *
 * If an implementation does not support a method, it may throw {@link UnsupportedOperationException}
 */
public interface MessageListener {
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
}
