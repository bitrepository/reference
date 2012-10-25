package org.bitrepository.protocol.messagebus.logger;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
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
            IdentifyPillarsForPutFileRequest request = (IdentifyPillarsForPutFileRequest)message;
            messageSB.append(" FileID=" + request.getFileID());
        }

        else if (message instanceof PutFileRequest) {
            PutFileRequest putFileRequest = (PutFileRequest)message;
            messageSB.append(" FileID=" + putFileRequest.getFileID());
            messageSB.append(", FileAddress=" + putFileRequest.getFileAddress());
            if (putFileRequest.getChecksumDataForNewFile() != null) {
                messageSB.append(", ChecksumDataForNewFile=" + putFileRequest.getChecksumDataForNewFile());
            }
            if (putFileRequest.getChecksumRequestForNewFile() != null) {
                messageSB.append(", ChecksumRequestForNewFile=" + putFileRequest.getChecksumRequestForNewFile());
            }
            if (putFileRequest.getFileSize() != null) {
                messageSB.append(", FileSize=" + putFileRequest.getFileSize());
            }
            if (putFileRequest.getAuditTrailInformation() != null) {
                messageSB.append(", AuditTrailInformation=" + putFileRequest.getAuditTrailInformation());
            }
        }

        else if (message instanceof PutFileFinalResponse) {
            PutFileFinalResponse response = (PutFileFinalResponse)message;
            if (response.getChecksumDataForNewFile() != null) {
                messageSB.append(", ChecksumDataForNewFile=" + response.getChecksumDataForNewFile());
            }
        }
        return messageSB;
    }
}
