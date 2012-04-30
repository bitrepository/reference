package org.bitrepository.common.utils;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;

/**
 * Utility class for making default responses.
 */
public class ResponseInfoUtils {
    /** Private constructor to prevent instantiation of this utility class.*/
    private ResponseInfoUtils() { }
    
    /**
     * The default message for identifying a contributor.
     * @return The ResponseInfo for telling that a contributor has been identified.
     */
    public static ResponseInfo getPositiveIdentification() {
        ResponseInfo ri = new ResponseInfo();
        ri.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        ri.setResponseText("Contributor identified for operation.");
        return ri;
    }
    
    /**
     * The default message for the initial progress response for a contributor.
     * @return The ResponseInfo for telling, that the operation has started.
     */
    public static ResponseInfo getInitialProgressResponse() {
        ResponseInfo ri = new ResponseInfo();
        ri.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        ri.setResponseText("Operation acknowledged and accepted.");
        return ri;
    }
}
