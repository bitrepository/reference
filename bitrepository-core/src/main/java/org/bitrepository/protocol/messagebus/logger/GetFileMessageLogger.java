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

import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.Message;

/**
 * Custom logger adding GetFile message specific parameters.
 */
public class GetFileMessageLogger extends DefaultMessagingLogger {
    @Override
    protected StringBuilder appendCustomInfo(StringBuilder messageSB, Message message) {
        if (message instanceof IdentifyPillarsForGetFileRequest) {
            IdentifyPillarsForGetFileRequest request = (IdentifyPillarsForGetFileRequest) message;
            messageSB.append(" FileID=" + request.getFileID());
        }

        else if (message instanceof GetFileRequest) {
            GetFileRequest getFileRequest = (GetFileRequest) message;
            messageSB.append(" FileID=" + getFileRequest.getFileID());
            if (getFileRequest.getFilePart() != null) {
                messageSB.append(", FilePart=" + getFileRequest.getFilePart());
            }
            messageSB.append(", FileAddress=" + getFileRequest.getFileAddress());
        }
        return messageSB;
    }
}
