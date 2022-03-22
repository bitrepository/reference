package org.bitrepository.protocol.messagebus.logger;

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

import org.bitrepository.bitrepositorymessages.*;

/**
 * Custom logger adding PutFile message specific parameters.
 */
public class PutFileMessageLogger extends DefaultMessagingLogger {
    @Override
    protected void appendCustomInfo(StringBuilder messageSB, Message message) {
        if (message instanceof IdentifyPillarsForPutFileRequest) {
            IdentifyPillarsForPutFileRequest request = (IdentifyPillarsForPutFileRequest) message;
            messageSB.append(" FileID=").append(request.getFileID());
        } else if (message instanceof IdentifyPillarsForPutFileResponse) {
            IdentifyPillarsForPutFileResponse response = (IdentifyPillarsForPutFileResponse) message;
            messageSB.append(" ChecksumDataForExistingFileID=").append(response.getChecksumDataForExistingFile());
        } else if (message instanceof PutFileRequest) {
            PutFileRequest request = (PutFileRequest) message;
            messageSB.append(" FileID=").append(request.getFileID());
            messageSB.append(", FileAddress=").append(request.getFileAddress());
            if (request.getChecksumDataForNewFile() != null) {
                messageSB.append(", ChecksumDataForNewFile=").append(request.getChecksumDataForNewFile());
            }
            if (request.getChecksumRequestForNewFile() != null) {
                messageSB.append(", ChecksumRequestForNewFile=").append(request.getChecksumRequestForNewFile());
            }
            if (request.getFileSize() != null) {
                messageSB.append(", FileSize=").append(request.getFileSize());
            }
            if (request.getAuditTrailInformation() != null) {
                messageSB.append(", AuditTrailInformation=").append(request.getAuditTrailInformation());
            }
        } else if (message instanceof PutFileFinalResponse) {
            PutFileFinalResponse response = (PutFileFinalResponse) message;
            if (response.getChecksumDataForNewFile() != null) {
                messageSB.append(", ChecksumDataForNewFile=").append(response.getChecksumDataForNewFile());
            }
        }
    }
}
