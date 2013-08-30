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

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;

/**
 * Custom logger adding PutFile message specific parameters.
 */
public class PutFileMessageLogger extends DefaultMessagingLogger {
    @Override
    protected StringBuilder appendCustomInfo(StringBuilder messageSB, Message message) {
        if (message instanceof IdentifyPillarsForPutFileRequest) {
            IdentifyPillarsForPutFileRequest request = (IdentifyPillarsForPutFileRequest) message;
            messageSB.append(" FileID=" + request.getFileID());
        } else if (message instanceof IdentifyPillarsForPutFileResponse) {
            IdentifyPillarsForPutFileResponse response = (IdentifyPillarsForPutFileResponse) message;
            messageSB.append(" ChecksumDataForExistingFileID=" + response.getChecksumDataForExistingFile());
        }

        else if (message instanceof PutFileRequest) {
            PutFileRequest request = (PutFileRequest) message;
            messageSB.append(" FileID=" + request.getFileID());
            messageSB.append(", FileAddress=" + request.getFileAddress());
            if (request.getChecksumDataForNewFile() != null) {
                messageSB.append(", ChecksumDataForNewFile=" + request.getChecksumDataForNewFile());
            }
            if (request.getChecksumRequestForNewFile() != null) {
                messageSB.append(", ChecksumRequestForNewFile=" + request.getChecksumRequestForNewFile());
            }
            if (request.getFileSize() != null) {
                messageSB.append(", FileSize=" + request.getFileSize());
            }
            if (request.getAuditTrailInformation() != null) {
                messageSB.append(", AuditTrailInformation=" + request.getAuditTrailInformation());
            }
        }

        else if (message instanceof PutFileFinalResponse) {
            PutFileFinalResponse response = (PutFileFinalResponse) message;
            if (response.getChecksumDataForNewFile() != null) {
                messageSB.append(", ChecksumDataForNewFile=" + response.getChecksumDataForNewFile());
            }
        }
        return messageSB;
    }
}
