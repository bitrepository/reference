/*
 * #%L
 * Bitmagasin integrationstest
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
package org.bitrepository.pillar;

import java.math.BigInteger;

import org.bitrepository.bitrepositorymessages.GetFileComplete;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileComplete;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileResponse;

/**
 * Class for generating the messages for the Reference Pillar.
 */
public class ReferencePillarMessageFactory {
    /** The pillar this message creator belongs to.*/
    ReferencePillar pillar;
    
    /**
     * Package protected constructor.
     */
    ReferencePillarMessageFactory(ReferencePillar refPillar) {
        pillar = refPillar;
    }
    
    /**
     * Creates a IdentifyPillarsForGetFileReply based on a 
     * IdentifyPillarsForGetFileRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - PillarChecksumType
     * <br/> - ReplyTo
     * 
     * @param msg The IdentifyPillarsForGetFileRequest to base the reply on.
     * @return The reply to the request.
     */
    public IdentifyPillarsForGetFileResponse createIdentifyPillarsForGetFileResponse(
            IdentifyPillarsForGetFileRequest msg) {
        IdentifyPillarsForGetFileResponse res 
                = new IdentifyPillarsForGetFileResponse();
        res.setCorrelationID(msg.getCorrelationID());
        res.setMinVersion(BigInteger.valueOf(1L));
        res.setVersion(BigInteger.valueOf(1L));
        res.setFileID(msg.getFileID());
        res.setPillarID(pillar.getPillarId());
        res.setSlaID(msg.getSlaID());
        return res;
    }
    
    /**
     * Creates a IdentifyPillarsForPutFileReply based on a 
     * IdentifyPillarsForPutFileRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - ChecksumType
     * <br/> - ReplyTo
     * 
     * @param msg The IdentifyPillarsForPutFileRequest to base the reply on.
     * @return A IdentifyPillarsForPutFileReply from the request.
     */
    public IdentifyPillarsForPutFileResponse createIdentifyPillarsForPutFileResponse(
            IdentifyPillarsForPutFileRequest msg) {
        IdentifyPillarsForPutFileResponse res
                = new IdentifyPillarsForPutFileResponse();
        res.setCorrelationID(msg.getCorrelationID());
        res.setMinVersion(BigInteger.valueOf(1L));
        res.setVersion(BigInteger.valueOf(1L));
//        res.setFileID(msg.getFileID());
        res.setSlaID(msg.getSlaID());
        res.setPillarID(pillar.getPillarId());
        
        return res;
    }
    
    /**
     * Creates a GetFileResponse based on a GetFileRequest. Missing the 
     * following fields:
     * <br/> - Cheksum
     * <br/> - FileAddress
     * <br/> - FileChecksumType
     * <br/> - PartLength
     * <br/> - PartOffSet
     * <br/> - PillarChecksumType
     * <br/> - ReplyTo
     * <br/> - ExpectedFileSize
     * <br/> - ResponseCode
     * <br/> - ResponseText
     * 
     * @param msg The GetFileRequest to base the response on.
     * @return The GetFileResponse based on the request.
     */
    public GetFileResponse createGetFileResponse(GetFileRequest msg) {
        GetFileResponse res = new GetFileResponse();
        res.setCorrelationID(msg.getCorrelationID());
        res.setMinVersion(BigInteger.valueOf(1L));
        res.setVersion(BigInteger.valueOf(1L));
        res.setFileID(msg.getFileID());
        res.setPillarID(pillar.getPillarId());
        res.setSlaID(msg.getSlaID());
        
        return res;
    }
    
    /**
     * Creates a GetFileComplete based on a GetFileRequest. Missing the 
     * following fields:
     * <br/> - FileAddress
     * <br/> - CompleteCode
     * <br/> - CompleteText
     * <br/> - PartLength
     * <br/> - PartOffSet
     * <br/> - PillarChecksumType
     * 
     * @param msg The GetFileRequest to base the reply for complete on.
     * @return The GetFileComplete based on the request.
     */
    public GetFileComplete createGetFileComplete(GetFileRequest msg) {
        GetFileComplete res = new GetFileComplete();
        res.setCorrelationID(msg.getCorrelationID());
        res.setMinVersion(BigInteger.valueOf(1L));
        res.setVersion(BigInteger.valueOf(1L));
        res.setFileID(msg.getFileID());
        res.setPillarID(pillar.getPillarId());
        res.setSlaID(msg.getSlaID());

        return res;
    }
    
    /**
     * Creates a PutFileResponse based on a PutFileRequest. Missing the 
     * following fields:
     * <br/> - ResponseCode
     * <br/> - ResponseText
     * <br/> - FileAddress
     * <br/> - PillarChecksumType
     * <br/> - ReplyTo
     * 
     * @param msg The PutFileRequest to base the response on.
     * @return The PutFileResponse based on the request.
     */
    public PutFileResponse createPutFileResponse(PutFileRequest msg) {
        PutFileResponse res = new PutFileResponse();
        res.setCorrelationID(msg.getCorrelationID());
        res.setVersion(BigInteger.valueOf(1L));
        res.setMinVersion(BigInteger.valueOf(1L));
        res.setPillarID(pillar.getPillarId());
        res.setSlaID(msg.getSlaID());
        
        return res;
    }

    /**
     * Creates a PutFileComplete based on a PutFileRequest. Missing the
     * following fields:
     * <br/> - CompleteCode
     * <br/> - CompleteText
     * <br/> - ReplyTo
     * <br/> - CompleteSaltChecksum
     * <br/> - FileAddress
     * <br/> - PillarChecksumType
     * 
     * @param msg The PutFileRequest to base the complete message on.
     * @return The PutFileComplete message based on the request.
     */
    public PutFileComplete createPutFileComplete(PutFileRequest msg) {
        PutFileComplete res = new PutFileComplete();
        res.setCorrelationID(msg.getCorrelationID());
        res.setFileID(msg.getFileID());
        res.setMinVersion(BigInteger.valueOf(1L));
        res.setVersion(BigInteger.valueOf(1L));
        res.setPillarID(pillar.getPillarId());
        res.setSlaID(msg.getSlaID());

        return res;
    }
}
