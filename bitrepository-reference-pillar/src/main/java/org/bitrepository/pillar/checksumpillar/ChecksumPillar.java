package org.bitrepository.pillar.checksumpillar;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.checksumpillar.cache.ChecksumCache;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The checksum pillar. 
 */
public class ChecksumPillar {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The messagebus for the pillar.*/
    private final MessageBus messageBus;
    
    private ChecksumCache cache;
    private Object mediator;
 
    public ChecksumPillar(MessageBus messageBus, Settings settings) {
        ArgumentValidator.checkNotNull(messageBus, "messageBus");
        ArgumentValidator.checkNotNull(settings, "settings");
        
        this.messageBus = messageBus;
        
        log.info("Starting the reference pillar!");
        
        log.info("ReferencePillar started!");
    }
}
