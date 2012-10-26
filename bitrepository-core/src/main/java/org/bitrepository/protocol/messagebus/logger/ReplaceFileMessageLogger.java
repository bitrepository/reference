package org.bitrepository.protocol.messagebus.logger;

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
