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

import javax.xml.bind.JAXBException;

import org.bitrepository.bitrepositorymessages.*;
import org.bitrepository.common.JaxbHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    protected void reportUnsupported(Object message) {
        JaxbHelper jaxbHelper = new JaxbHelper("xsd/", "BitRepositoryMessages.xsd");
        try {
            log.warn("Received unsupported message '{}'", jaxbHelper.serializeToXml(message));
        } catch (JAXBException e) {
            log.warn("Received unsupported message of type '" + message.getClass().getName()
                             + "', which could not be serialized as XML.", e);
        }
        throw new UnsupportedOperationException(
                "The message listener does not accept messages of this type: '" + message.getClass().getName() + "'");
    }
    
    @Override
    public void onMessage(AlarmMessage message) {
        reportUnsupported(message);
    }
    
    @Override
    public void onMessage(DeleteFileFinalResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(DeleteFileProgressResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(DeleteFileRequest message) {
        reportUnsupported(message);
    }
    
    @Override
    public void onMessage(GetAuditTrailsFinalResponse message) {
        reportUnsupported(message);
    }
    
    @Override
    public void onMessage(GetAuditTrailsProgressResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetAuditTrailsRequest message) {
        reportUnsupported(message);
    }
    
    @Override
    public void onMessage(GetChecksumsFinalResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetChecksumsRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetChecksumsProgressResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetFileFinalResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetFileIDsFinalResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetFileIDsRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetFileIDsProgressResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetFileRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetFileProgressResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(GetStatusRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForDeleteFileRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForDeleteFileResponse message) {
        reportUnsupported(message);
    }
    
    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyPillarsForReplaceFileRequest message) {
        reportUnsupported(message);
    }
    
    @Override
    public void onMessage(IdentifyPillarsForReplaceFileResponse message) {
        reportUnsupported(message);
    }
    
    @Override
    public void onMessage(IdentifyContributorsForGetAuditTrailsRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(IdentifyContributorsForGetAuditTrailsResponse message) {
        reportUnsupported(message);
    }
    
    @Override
    public void onMessage(IdentifyContributorsForGetStatusRequest message) {
        reportUnsupported(message);
    }
  
    @Override
    public void onMessage(PutFileFinalResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(PutFileRequest message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(PutFileProgressResponse message) {
        reportUnsupported(message);
    }
    
    @Override
    public void onMessage(ReplaceFileRequest message) {
        reportUnsupported(message);
    }
    
    @Override
    public void onMessage(ReplaceFileFinalResponse message) {
        reportUnsupported(message);
    }
    
    @Override
    public void onMessage(ReplaceFileProgressResponse message) {
        reportUnsupported(message);
    }

    @Override
    public void onMessage(Message message) {
        reportUnsupported(message);
    }
}
