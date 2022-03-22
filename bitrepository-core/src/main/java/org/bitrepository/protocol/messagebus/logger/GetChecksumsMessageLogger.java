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

import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.common.utils.FileIDsUtils;

/**
 * Custom logger adding GetChecksums message specific parameters.
 */
public class GetChecksumsMessageLogger extends DefaultMessagingLogger {
    @Override
    protected void appendCustomInfo(StringBuilder messageSB, Message message) {
        if (message instanceof IdentifyPillarsForGetChecksumsRequest) {
            IdentifyPillarsForGetChecksumsRequest request = (IdentifyPillarsForGetChecksumsRequest) message;
            messageSB.append(" FileIDs=").append(FileIDsUtils.asString(request.getFileIDs()));
        } else if (message instanceof GetChecksumsRequest) {
            GetChecksumsRequest request = (GetChecksumsRequest) message;
            messageSB.append(" FileIDs=").append(FileIDsUtils.asString(request.getFileIDs()));
            if (request.getChecksumRequestForExistingFile() != null) {
                messageSB.append(", ChecksumRequestForExistingFile=").append(request.getChecksumRequestForExistingFile());
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
        } else if (message instanceof GetChecksumsFinalResponse) {
            GetChecksumsFinalResponse response = (GetChecksumsFinalResponse) message;

            if (response.getResultingChecksums() != null && response.getResultingChecksums().getChecksumDataItems() != null) {
                messageSB.append(", NumberOfChecksums=").append(response.getResultingChecksums().getChecksumDataItems().size());
            }

            if (response.getChecksumRequestForExistingFile() != null) {
                messageSB.append(", ChecksumRequestForExistingFile=").append(response.getChecksumRequestForExistingFile());
            }

            if (response.isPartialResult() != null) {
                messageSB.append(", PartialResult=").append(response.isPartialResult());
            }
        }
    }
}
