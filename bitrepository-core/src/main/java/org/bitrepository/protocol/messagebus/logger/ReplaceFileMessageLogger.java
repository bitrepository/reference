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

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;

/**
 * Custom logger adding ReplaceFile message specific parameters.
 */
public class ReplaceFileMessageLogger extends DefaultMessagingLogger {
    @Override
    protected StringBuilder appendCustomInfo(StringBuilder messageSB, Message message) {
        if (message instanceof IdentifyPillarsForReplaceFileRequest) {
            IdentifyPillarsForReplaceFileRequest request = (IdentifyPillarsForReplaceFileRequest) message;
            messageSB.append(" FileID=" + request.getFileID());
        }

        else if (message instanceof ReplaceFileRequest) {
            ReplaceFileRequest request = (ReplaceFileRequest) message;
            messageSB.append(" FileID=" + request.getFileID());
            messageSB.append(", FileAddress=" + request.getFileAddress());
            if (request.getChecksumDataForNewFile() != null) {
                messageSB.append(", ChecksumDataForNewFile=" + request.getChecksumDataForNewFile());
            }
            if (request.getChecksumRequestForNewFile() != null) {
                messageSB.append(", ChecksumRequestForNewFile=" + request.getChecksumRequestForNewFile());
            }
            if (request.getChecksumDataForExistingFile() != null) {
                messageSB.append(", ChecksumDataForExistingFile=" + request.getChecksumDataForExistingFile());
            }
            if (request.getChecksumRequestForExistingFile() != null) {
                messageSB.append(", ChecksumRequestForExistingFile=" + request.getChecksumRequestForExistingFile());
            }
            if (request.getFileSize() != null) {
                messageSB.append(", FileSize=" + request.getFileSize());
            }
            if (request.getAuditTrailInformation() != null) {
                messageSB.append(", AuditTrailInformation=" + request.getAuditTrailInformation());
            }
        }

        else if (message instanceof ReplaceFileFinalResponse) {
            ReplaceFileFinalResponse response = (ReplaceFileFinalResponse) message;
            if (response.getChecksumDataForExistingFile() != null) {
                messageSB.append(", ChecksumDataForExistingFile=" + response.getChecksumDataForExistingFile());
            }
            if (response.getChecksumDataForNewFile() != null) {
                messageSB.append(", ChecksumDataForNewFile=" + response.getChecksumDataForNewFile());
            }
        }
        return messageSB;
    }
}
