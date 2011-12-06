package org.bitrepository.pillar.exceptions;

import org.bitrepository.bitrepositoryelements.ResponseInfo;

/**
 * Exception which wraps bad response information for the validation of the operation requests. 
 */
public class InvalidMessageException extends RuntimeException {
    /** The ResponseInfo wrapped by this exception. Tells the reason for the exception.*/
    private final ResponseInfo responseInfo;
    
    /**
     * Constructor.
     * @param rInfo The IdentifyResponseInfo for this class to wrap.
     */
    public InvalidMessageException(ResponseInfo rInfo) {
        super(rInfo.getResponseText());
        responseInfo = rInfo;
    }
    
    /**
     * Constructor.
     * @param irInfo The IdentifyResponseInfo for this class to wrap.
     * @param e The exception to wrap into the StackTrace.
     */
    public InvalidMessageException(ResponseInfo rInfo, Exception e) {
        super(rInfo.getResponseText(), e);
        responseInfo = rInfo;
    }
    
    /**
     * @return The wrapped ResponseInfo.
     */
    public ResponseInfo getResponseInfo() {
        return responseInfo;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", " + responseInfo.toString();
    }
}
