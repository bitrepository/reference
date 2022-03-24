package org.bitrepository.protocol.messagebus.logger;

/*
 * #%L
 * BitRepository Core
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

import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.common.utils.FileIDsUtils;

/**
 * Custom logger adding GetFileIDs message specific parameters.
 */
public class GetFileIDsMessageLogger extends DefaultMessagingLogger {
    @Override
    protected void appendCustomInfo(StringBuilder messageSB, Message message) {
        if (message instanceof IdentifyPillarsForGetFileIDsRequest) {
            IdentifyPillarsForGetFileIDsRequest request = (IdentifyPillarsForGetFileIDsRequest) message;
            if (request.getFileIDs() != null) {
                messageSB.append(" FileIDs=").append(FileIDsUtils.asString(request.getFileIDs()));
            }
        } else if (message instanceof GetFileIDsRequest) {
            GetFileIDsRequest request = (GetFileIDsRequest) message;
            if (request.getFileIDs() != null) {
                messageSB.append(" FileIDs=").append(FileIDsUtils.asString(request.getFileIDs()));
            }
            if (request.getResultAddress() != null) {
                messageSB.append(", FileAddress=").append(request.getResultAddress());
            }
            if (request.getMaxNumberOfResults() != null) {
                messageSB.append(", MaxNumberOfResults=").append(request.getMaxNumberOfResults());
            }
            if (request.getMaxTimestamp() != null) {
                messageSB.append(", MaxTimestamp=").append(request.getMaxTimestamp());
            }
            if (request.getMinTimestamp() != null) {
                messageSB.append(", MinTimestamp=").append(request.getMinTimestamp());
            }
            if (request.getAuditTrailInformation() != null) {
                messageSB.append(", AuditTrailInformation=").append(request.getAuditTrailInformation());
            }
        } else if (message instanceof GetFileIDsFinalResponse) {
            GetFileIDsFinalResponse response = (GetFileIDsFinalResponse) message;

            if (response.getResultingFileIDs() != null && response.getResultingFileIDs().getFileIDsData() != null) {
                messageSB.append(", NumberOfFileIDs=")
                        .append(response.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size());
            }

            if (response.isPartialResult() != null) {
                messageSB.append(", PartialResult=").append(response.isPartialResult());
            }
        }
    }
}
