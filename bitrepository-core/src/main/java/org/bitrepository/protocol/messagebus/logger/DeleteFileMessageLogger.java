package org.bitrepository.protocol.messagebus.logger;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;

/**
 * Custom logger adding DeleteFile message specific parameters.
 */
public class DeleteFileMessageLogger extends DefaultMessagingLogger {
    @Override
    protected StringBuilder appendCustomInfo(StringBuilder messageSB, Message message) {
        if (message instanceof IdentifyPillarsForDeleteFileRequest) {
            IdentifyPillarsForDeleteFileRequest request = (IdentifyPillarsForDeleteFileRequest) message;
            messageSB.append(" FileID=" + request.getFileID());
        }

        else if (message instanceof DeleteFileRequest) {
            DeleteFileRequest request = (DeleteFileRequest) message;
            messageSB.append(" FileID=" + request.getFileID());
            if (request.getChecksumDataForExistingFile() != null) {
                messageSB.append(", ChecksumDataForExistingFile=" + request.getChecksumDataForExistingFile());
            }
            if (request.getChecksumRequestForExistingFile() != null) {
                messageSB.append(", ChecksumRequestForExistingFile=" + request.getChecksumRequestForExistingFile());
            }
            if (request.getAuditTrailInformation() != null) {
                messageSB.append(", AuditTrailInformation=" + request.getAuditTrailInformation());
            }
        }

        else if (message instanceof DeleteFileFinalResponse) {
            DeleteFileFinalResponse response = (DeleteFileFinalResponse) message;
            if (response.getChecksumDataForExistingFile() != null) {
                messageSB.append(", ChecksumDataForExistingFile=" + response.getChecksumDataForExistingFile());
            }
        }
        return messageSB;
    }
}
