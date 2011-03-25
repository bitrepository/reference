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
import org.bitrepository.common.JaxbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.JAXBException;

/**
 * Abstract implementation of message listener.
 *
 * This implementation will log received messages as warnings, and throw an
 * exception about these being unsupported.
 */
public abstract class AbstractMessageListener implements MessageListener {
    /** Logger for this class. */
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Report an unsupported message by logging a warning and throwing an exception.
     *
     * @param message The unsupported message received.
     */
    private void reportUnsupported(Object message) {
        try {
            log.warn("Received unsupported message '{}'", JaxbHelper.serializeToXml(message));
        } catch (JAXBException e) {
            log.warn("Received unsupported message of type '" + message.getClass().getName()
                             + "', which could not be serialized as XML.", e);
        }
        throw new UnsupportedOperationException(
                "The message listener does not accept messages of this type: '" + message.getClass().getName() + "'");
    }

    @Override
    public void onMessage(GetChecksumsComplete message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetChecksumsRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetChecksumsResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetFileComplete message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetFileIDsComplete message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetFileIDsRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetFileIDsResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetFileRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetFileResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsReply message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsReply message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileReply message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileReply message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(PutFileComplete message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(PutFileRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(PutFileResponse message) {
        reportUnsupported(message);
    }
}
