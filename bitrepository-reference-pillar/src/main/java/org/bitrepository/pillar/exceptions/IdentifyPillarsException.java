package org.bitrepository.pillar.exceptions;

import org.bitrepository.bitrepositoryelements.IdentifyResponseInfo;

/**
 * Exception which wraps bad response information for the identifications. 
 */
public class IdentifyPillarsException extends RuntimeException {
    /** The IdentifyResponseInfo wrapped by this exception. Tells the reason for the exception.*/
    private final IdentifyResponseInfo identifyResponseInfo;
    
    /**
     * Constructor.
     * @param irInfo The IdentifyResponseInfo for this class to wrap.
     */
    public IdentifyPillarsException(IdentifyResponseInfo irInfo) {
        super(irInfo.getIdentifyResponseText());
        identifyResponseInfo = irInfo;
    }
    
    /**
     * Constructor.
     * @param irInfo The IdentifyResponseInfo for this class to wrap.
     * @param e The exception to wrap into the StackTrace.
     */
    public IdentifyPillarsException(IdentifyResponseInfo irInfo, Exception e) {
        super(irInfo.getIdentifyResponseText(), e);
        identifyResponseInfo = irInfo;
    }
    
    /**
     * @return The wrapped IdentifyResponseInfo.
     */
    public IdentifyResponseInfo getResponseInfo() {
        return identifyResponseInfo;
    }
    
    @Override
    public String toString() {
        return super.toString() + ", " + identifyResponseInfo.toString();
    }
}
