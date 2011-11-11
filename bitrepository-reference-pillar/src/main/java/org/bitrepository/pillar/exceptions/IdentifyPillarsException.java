package org.bitrepository.pillar.exceptions;

import org.bitrepository.bitrepositoryelements.ResponseInfo;

/**
 * Exception which wraps bad response information for the identifications. 
 */
public class IdentifyPillarsException extends RuntimeException {
    /** The IdentifyResponseInfo wrapped by this exception. Tells the reason for the exception.*/
    private final ResponseInfo identifyResponseInfo;
    
    /**
     * Constructor.
     * @param irInfo The IdentifyResponseInfo for this class to wrap.
     */
    public IdentifyPillarsException(ResponseInfo irInfo) {
        super(irInfo.getResponseText());
        identifyResponseInfo = irInfo;
    }
    
    /**
     * Constructor.
     * @param irInfo The IdentifyResponseInfo for this class to wrap.
     * @param e The exception to wrap into the StackTrace.
     */
    public IdentifyPillarsException(ResponseInfo irInfo, Exception e) {
        super(irInfo.getResponseText(), e);
        identifyResponseInfo = irInfo;
    }
    
    /**
     * @return The wrapped IdentifyResponseInfo.
     */
    public ResponseInfo getResponseInfo() {
        return identifyResponseInfo;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", " + identifyResponseInfo.toString();
    }
}
