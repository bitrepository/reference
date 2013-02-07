/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.MessageResponse;

public class ResponsePopulator {
    protected final String collectionID;
    protected final String from;
    protected final String replyTo;


    public ResponsePopulator(String collectionID, String from, String replyTo) {
        this.collectionID = collectionID;
        this.from = from;
        this.replyTo = replyTo;
    }

    protected void initializeMessageDetails(Message msg) {
        msg.setCollectionID(collectionID);
        msg.setVersion(ProtocolVersionLoader.loadProtocolVersion().getVersion());
        msg.setMinVersion(ProtocolVersionLoader.loadProtocolVersion().getMinVersion());
    }

    protected void initialiseResponseDetails(
            MessageResponse response,
            String correlationID, String to) {
        initializeMessageDetails(response);
        response.setCorrelationID(correlationID);
        response.setDestination(to);
        response.setReplyTo(replyTo);
        response.setFrom(from);
        response.setResponseInfo(new ResponseInfo());
    }

    public void initialisePositiveIdentifyResponse(
            MessageResponse response,
            String correlationID, String to) {
        initialiseResponseDetails(response,correlationID, to);
        ResponseInfo info = new ResponseInfo();
        info.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        info.setResponseText("Ready to service ");
    }

}
