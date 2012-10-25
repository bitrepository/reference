package org.bitrepository.protocol.messagebus.logger;

import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.Message;

/**
 * Custom logger adding GetAuditTrails message specific parameters.
 */
public class GetAuditTrailsMessageLogger extends DefaultMessagingLogger {
    @Override
    protected StringBuilder appendCustomInfo(StringBuilder messageSB, Message message) {
        if (message instanceof GetAuditTrailsRequest) {
            GetAuditTrailsRequest request = (GetAuditTrailsRequest)message;
            if (request.getResultAddress() != null) {
                messageSB.append(", FileAddress=" + request.getResultAddress());
            }
            if (request.getFileID() != null) {
                messageSB.append(", FileID=" + request.getFileID());
            }
            if (request.getMaxNumberOfResults() != null) {
                messageSB.append(", MaxNumberOfResults=" + request.getMaxNumberOfResults());
            }
            if (request.getMaxSequenceNumber() != null) {
                messageSB.append(", MaxSequenceNumber=" + request.getMaxSequenceNumber());
            }
            if (request.getMinSequenceNumber() != null) {
                messageSB.append(", MinSequenceNumber=" + request.getMinSequenceNumber());
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

        else if (message instanceof GetAuditTrailsFinalResponse) {
            GetAuditTrailsFinalResponse response = (GetAuditTrailsFinalResponse)message;

            if (response.getResultingAuditTrails() != null) {
                messageSB.append(", NumberOfAuditTrailEvents=" +
                    response.getResultingAuditTrails().getAuditTrailEvents().getAuditTrailEvent().size());
                }
            }
        return messageSB;
    }
}
