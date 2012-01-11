package org.bitrepository.access.audittrails.client;

import java.util.List;

import org.bitrepository.access.audittrails.ComponentDestination;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.AbstractMessageListener;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * Provides functionality for retrieving the list of available at a given time.
 */
public class AuditTrailContributorProvider {
    private final List<String> definedContributorIDs;
    private AuditTrailContributorDetector contributorDetector;
       
    public AuditTrailContributorProvider(Settings settings, MessageSender messageSender) {
        this.definedContributorIDs = settings.getReferenceSettings().getAuditTrailServiceSettings().getContributors();
        contributorDetector = new AuditTrailContributorDetector(messageSender);
    }

    /**
     * Returns the contributors currently available. A identify contributors request is used to lookup the contributors 
     * each time this method is called. 
     * @return The component with responded to a '
     */
    public ComponentDestination[] getAvailableContributors() {
        return contributorDetector.lookupContributors();
    }
    
    public List<String> getDefinedContributors() {
        return definedContributorIDs;
    }
    
    private class AuditTrailContributorDetector extends AbstractMessageListener {
        private final MessageSender messageSender;
        
        public AuditTrailContributorDetector(MessageSender messageSender) {
            this.messageSender = messageSender;
        }
        
        private synchronized ComponentDestination[] lookupContributors() {
            
            return null;
        }
        
        
    }
}
