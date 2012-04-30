package org.bitrepository.service.exception;

import org.bitrepository.bitrepositoryelements.ResponseInfo;

/**
 * The exception for the request handlers.
 */
@SuppressWarnings("serial")
public abstract class RequestHandlerException extends Exception {
    /** The ResponseInfo wrapped by this exception. Tells the reason for the exception.*/
    private final ResponseInfo responseInfo;
    
    /**
     * Constructor.
     * @param rInfo The ResponseInfo for this class to wrap.
     */
    public RequestHandlerException(ResponseInfo rInfo) {
        super(rInfo.getResponseText());
        responseInfo = rInfo;
    }
    
    /**
     * Constructor.
     * @param rInfo The ResponseInfo for this class to wrap.
     * @param e The exception to wrap into the StackTrace.
     */
    public RequestHandlerException(ResponseInfo rInfo, Exception e) {
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
