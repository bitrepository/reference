package org.bitrepository.protocol;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocolversiondefinition.ProtocolVersionDefinition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle message version validation for implementation of versioning support.  
 */
public class MessageVersionValidator {

    private static final Logger log = LoggerFactory.getLogger(MessageVersionValidator.class);
    private static final ProtocolVersionDefinition protocolVersion = new ProtocolVersionDefinition();
    
    private MessageVersionValidator() {}
    
    public static void validateMessageVersion(Message message) throws InvalidMessageVersionException {
        if(message.getMinVersion().compareTo(protocolVersion.getVersion()) > 0) {
            log.info("The Request minimum version is LARGER than this components version = " + protocolVersion.getVersion());
            throw new InvalidMessageVersionException(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE, 
                    "The requested minimum version is too large, components version: " + protocolVersion.getVersion());
        }
        if(message.getVersion() != null && message.getVersion().compareTo(protocolVersion.getMinVersion()) < 0) {
            log.info("The Request version is SMALLER than this components minimum version = " +
                    protocolVersion.getMinVersion());
            throw new InvalidMessageVersionException(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE,
                    "The requested version is smaller than the components minimum version, pillar minVersion: " +
                    protocolVersion.getMinVersion());
        }
    }
}
