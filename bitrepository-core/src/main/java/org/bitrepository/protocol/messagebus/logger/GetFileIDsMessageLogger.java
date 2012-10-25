package org.bitrepository.protocol.messagebus.logger;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.common.utils.FileIDsUtils;

/**
 * Custom logger adding GetFileIDs message specific parameters.
 */
public class GetFileIDsMessageLogger extends DefaultMessagingLogger {
    @Override
    protected StringBuilder appendCustomInfo(StringBuilder messageSB, Message message) {
        if (message instanceof IdentifyPillarsForGetFileIDsRequest) {
            IdentifyPillarsForGetFileIDsRequest request = (IdentifyPillarsForGetFileIDsRequest)message;
            if (request.getFileIDs() != null) {
                messageSB.append(" FileIDs=" + FileIDsUtils.asString(request.getFileIDs()));
            }
        }

        else if (message instanceof GetFileIDsRequest) {
                GetFileIDsRequest request = (GetFileIDsRequest)message;
            if (request.getFileIDs() != null) {
                messageSB.append(" FileIDs=" + FileIDsUtils.asString(request.getFileIDs()));
            }
            if (request.getResultAddress() != null) {
                messageSB.append(", FileAddress=" + request.getResultAddress());
            }
            if (request.getMaxNumberOfResults() != null) {
                messageSB.append(", MaxNumberOfResults=" + request.getMaxNumberOfResults());
            }
            if (request.getMaxTimestamp() != null) {
                messageSB.append(", MaxTimestamp=" + request.getMaxTimestamp());
            }
            if (request.getMinTimestamp() != null) {
                messageSB.append(", MinTimestamp=" + request.getMinTimestamp());
            }
            if (request.getAuditTrailInformation() != null) {
                messageSB.append(", AuditTrailInformation=" + request.getAuditTrailInformation());
            }
        }

        else if (message instanceof GetFileIDsFinalResponse) {
            GetFileIDsFinalResponse response = (GetFileIDsFinalResponse)message;

            if (response.getResultingFileIDs() != null && response.getResultingFileIDs().getFileIDsData() != null) {
                messageSB.append(", NumberOfFileIDs=" +
                    response.getResultingFileIDs().getFileIDsData().getFileIDsDataItems().getFileIDsDataItem().size());
                }
            }
        return messageSB;
    }
}
