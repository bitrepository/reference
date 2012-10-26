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
            IdentifyPillarsForPutFileRequest request = (IdentifyPillarsForPutFileRequest) message;
            messageSB.append(" FileID=" + request.getFileID());
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
