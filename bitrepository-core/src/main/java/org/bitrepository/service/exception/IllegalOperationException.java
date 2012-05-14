package org.bitrepository.service.exception;

import org.bitrepository.bitrepositoryelements.ResponseInfo;

/**
 * Exception for telling, that a given operation is illegal.
 * This might involve: deleting with a invalid checksum, performing the 'Get' operation on a ChecksumPillar, etc.
 */
@SuppressWarnings("serial")
public class IllegalOperationException extends RequestHandlerException {
    /**
     * Constructor.
     * @param rInfo The ResponseInfo for this class to wrap.
     */
    public IllegalOperationException(ResponseInfo rInfo) {
        super(rInfo);
    }
    
    /**
     * Constructor.
     * @param rInfo The ResponseInfo for this class to wrap.
     * @param e The exception to wrap into the StackTrace.
     */
    public IllegalOperationException(ResponseInfo rInfo, Exception e) {
        super(rInfo, e);
    }
}
