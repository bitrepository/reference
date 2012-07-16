package org.bitrepository.protocol;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;

public class InvalidMessageVersionException extends RuntimeException {

    private ResponseInfo response;
    
    public InvalidMessageVersionException(ResponseCode responseCode, String responseText) {
    	super(responseText);
        response = new ResponseInfo();
        response.setResponseCode(responseCode);
        response.setResponseText(responseText);
    }
    
    public ResponseInfo getResponseInfo() {
        return response;
    }
    
}
