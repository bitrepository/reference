package org.bitrepository.protocol.messagebus.logger;

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
            IdentifyPillarsForGetFileRequest request = (IdentifyPillarsForGetFileRequest)message;
            messageSB.append(" FileID=" + request.getFileID());
        }

        else if (message instanceof GetFileRequest) {
            GetFileRequest getFileRequest = (GetFileRequest)message;
            messageSB.append(" FileID=" + getFileRequest.getFileID());
            if (getFileRequest.getFilePart() != null) {
                messageSB.append(", FilePart=" + getFileRequest.getFilePart());
            }
            messageSB.append(", FileAddress=" + getFileRequest.getFileAddress());
        }
        return messageSB;
    }
}
