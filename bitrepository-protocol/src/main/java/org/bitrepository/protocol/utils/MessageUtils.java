package org.bitrepository.protocol.utils;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.MessageResponse;

public class MessageUtils {
    private MessageUtils() {}

    public static boolean isPositiveResponse (MessageResponse response) {
        ResponseCode responseCode = response.getResponseInfo().getResponseCode();
        return responseCode.equals(ResponseCode.IDENTIFICATION_POSITIVE) ||
               responseCode.equals(ResponseCode.OPERATION_ACCEPTED_PROGRESS) ||
               responseCode.equals(ResponseCode.OPERATION_COMPLETED);
    }
}
