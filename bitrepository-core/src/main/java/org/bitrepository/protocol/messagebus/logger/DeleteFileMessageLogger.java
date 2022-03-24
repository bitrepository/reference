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

import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.Message;

/**
 * Custom logger adding DeleteFile message specific parameters.
 */
public class DeleteFileMessageLogger extends DefaultMessagingLogger {
    @Override
    protected void appendCustomInfo(StringBuilder messageSB, Message message) {
        if (message instanceof IdentifyPillarsForDeleteFileRequest) {
            IdentifyPillarsForDeleteFileRequest request = (IdentifyPillarsForDeleteFileRequest) message;
            messageSB.append(" FileID=").append(request.getFileID());
        } else if (message instanceof DeleteFileRequest) {
            DeleteFileRequest request = (DeleteFileRequest) message;
            messageSB.append(" FileID=").append(request.getFileID());
            if (request.getChecksumDataForExistingFile() != null) {
                messageSB.append(", ChecksumDataForExistingFile=").append(request.getChecksumDataForExistingFile());
            }
            if (request.getChecksumRequestForExistingFile() != null) {
                messageSB.append(", ChecksumRequestForExistingFile=").append(request.getChecksumRequestForExistingFile());
            }
            if (request.getAuditTrailInformation() != null) {
                messageSB.append(", AuditTrailInformation=").append(request.getAuditTrailInformation());
            }
        } else if (message instanceof DeleteFileFinalResponse) {
            DeleteFileFinalResponse response = (DeleteFileFinalResponse) message;
            if (response.getChecksumDataForExistingFile() != null) {
                messageSB.append(", ChecksumDataForExistingFile=").append(response.getChecksumDataForExistingFile());
            }
        }
    }
}
