package org.bitrepository.protocol.messagebus.logger;

import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.common.utils.FileIDsUtils;

/**
 * Custom logger adding GetChecksums message specific parameters.
 */
public class GetChecksumsMessageLogger extends DefaultMessagingLogger {
    @Override
    protected StringBuilder appendCustomInfo(StringBuilder messageSB, Message message) {
        if (message instanceof IdentifyPillarsForGetChecksumsRequest) {
            IdentifyPillarsForGetChecksumsRequest request = (IdentifyPillarsForGetChecksumsRequest)message;
            messageSB.append(" FileIDs=" + FileIDsUtils.asString(request.getFileIDs()));
        }

        else if (message instanceof GetChecksumsRequest) {
            GetChecksumsRequest request = (GetChecksumsRequest)message;
            messageSB.append(" FileIDs=" + FileIDsUtils.asString(request.getFileIDs()));
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

        else if (message instanceof GetChecksumsFinalResponse) {
            GetChecksumsFinalResponse response = (GetChecksumsFinalResponse)message;

            if (response.getResultingChecksums() != null && response.getResultingChecksums().getChecksumDataItems() != null) {
                messageSB.append(", NumberOfChecksums=" +
                        response.getResultingChecksums().getChecksumDataItems().size());
                }
            }
        return messageSB;
    }
}
