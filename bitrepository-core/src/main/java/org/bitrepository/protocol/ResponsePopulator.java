package org.bitrepository.protocol;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.MessageResponse;

import java.math.BigInteger;

public class ResponsePopulator {
    protected final String collectionID;
    protected final String from;
    protected final String replyTo;


    public ResponsePopulator(String collectionID, String from, String replyTo) {
        this.collectionID = collectionID;
        this.from = from;
        this.replyTo = replyTo;
    }

    protected void initializeMessageDetails(Message msg) {
        msg.setCollectionID(collectionID);
        msg.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        msg.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
    }

    protected void initialiseResponseDetails(
            MessageResponse response,
            String correlationID, String to) {
        initializeMessageDetails(response);
        response.setCorrelationID(correlationID);
        response.setTo(to);
        response.setReplyTo(replyTo);
        response.setFrom(from);
        response.setResponseInfo(new ResponseInfo());
    }

    public void initialisePositiveIdentifyResponse(
            MessageResponse response,
            String correlationID, String to) {
        initialiseResponseDetails(response,correlationID, to);
        ResponseInfo info = new ResponseInfo();
        info.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        info.setResponseText("Ready to service ");
    }

}
