package org.bitrepository.protocol;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.Message;



/**
 * Class to handle message version validation for implementation of versioning support.  
 */
public class MessageVersionValidator {

    private MessageVersionValidator() {}
    
    public static void validateMessageVersion(Message message) throws InvalidMessageVersionException {
        /*if(message.getMinVersion().compareTo(context.protocolVersion) > 0) {
            log.info("The Request minimum version is LARGER than this pillars version = " + context.protocolVersion);
            throw new InvalidMessageVersionException(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE, 
                    "The requested minimum version is too large, pillar version: "+ context.protocolVersion, 
                    "Received message containing a message with an invalid message version.");
        }
        if(version != null && version.compareTo(context.protocolMinVersion) < 0) {
            log.info("The Request version is SMALLER than this pillars minimum version = " + context.protocolMinVersion);
            throw new InvalidMessageVersionException(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE,
                    "The requested version is smaller than the pillars minimum version, pillar minVersion: " +
            context.protocolMinVersion, "Received message containing a message with an invalid message version.");
        }*/
    }
}
