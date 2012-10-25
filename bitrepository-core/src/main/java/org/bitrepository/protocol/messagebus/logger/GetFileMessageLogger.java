package org.bitrepository.protocol.messagebus.logger;

import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.Message;

public class GetFileMessageLogger extends DefaultMessagingLogger {
    @Override
    protected StringBuilder appendCustomInfo(StringBuilder messageSB, Message message) {
        if (message instanceof GetFileRequest) {
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
