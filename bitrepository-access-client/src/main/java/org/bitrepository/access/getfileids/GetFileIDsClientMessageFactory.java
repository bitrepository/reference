/*
 * #%L
 * Bitrepository Access Client
 * *
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
/*
 * org.bitrepository.access
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
package org.bitrepository.access.getfileids;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;

import java.math.BigInteger;

/**
 * GetFileIDsClientMessageFactory generates request messages for BasicGetFileIDsClient.
 */
public class GetFileIDsClientMessageFactory {

    private static final BigInteger VERSION_DEFAULT = BigInteger.valueOf(1L);

    /**
     * Prevent initialisation - currently a utility class.
     */
    private GetFileIDsClientMessageFactory() {}

    /**
     * Generate IdentifyPillarsForGetFileIDsRequest message with specified values.
     * @param correlationID
     * @param slaID
     * @param replyTo
     * @param fileIDs
     * @return test message
     */
    public static IdentifyPillarsForGetFileIDsRequest getIdentifyPillarsForGetFileIDsRequestMessage(
            String correlationID, String slaID, String replyTo, FileIDs fileIDs) {
        IdentifyPillarsForGetFileIDsRequest request = new IdentifyPillarsForGetFileIDsRequest();
        request.setCorrelationID(correlationID);
        request.setBitrepositoryContextID(slaID);
        request.setReplyTo(replyTo);
        if (fileIDs == null) {
            fileIDs = new FileIDs();
            fileIDs.setAllFileIDs("Why is this a String? Change to Boolean?");
        }
        request.setFileIDs(fileIDs);
        request.setVersion(VERSION_DEFAULT);
        request.setMinVersion(VERSION_DEFAULT);
        return request;
    }

    /**
     * Generate GetFileIDsRequest message with specified values.
     * @param correlationID
     * @param slaID
     * @param replyTo
     * @param pillarID
     * @param resultAddress
     * @param fileIDs
     * @return
     */
    public static GetFileIDsRequest getGetFileIDsRequestMessage(
            String correlationID, String slaID, String replyTo, String pillarID,
            String resultAddress, FileIDs fileIDs) {
        GetFileIDsRequest request = new GetFileIDsRequest();
        request.setCorrelationID(correlationID);
        request.setBitrepositoryContextID(slaID);
        request.setReplyTo(replyTo);
        request.setPillarID(pillarID);
        if (resultAddress != null) {
            request.setResultAddress(resultAddress);
        }
        if (fileIDs == null) {
            fileIDs = new FileIDs();
            fileIDs.setAllFileIDs("Why is this a String? Change to Boolean?");
        }
        request.setFileIDs(fileIDs);
        request.setVersion(VERSION_DEFAULT);
        request.setMinVersion(VERSION_DEFAULT);
        return request;
    }
}
