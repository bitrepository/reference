package org.bitrepository.pillar.common;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.AlarmDispatcher;
import org.bitrepository.pillar.AuditTrailManager;
import org.bitrepository.protocol.messagebus.MessageBus;

/**
 * Container for the context of the pillar, e.g. all the components needed for the message handling.
 */
public class PillarContext {
    /** The settings.*/
    private final Settings settings;
    /** The message bus.*/
    private final MessageBus messageBus;
    /** The alarm dispatcher.*/
    private final AlarmDispatcher alarmDispatcher;
    /** The audit trail manager.*/
    private final AuditTrailManager auditTrailManager;
    
    /**
     * Constructor.
     * @param settings The settings.
     * @param messageBus The message bus.
     * @param alarmDispatcher The alarm dispatcher.
     * @param auditTrailManager The audit trial manager.
     */
    public PillarContext(Settings settings, MessageBus messageBus, AlarmDispatcher alarmDispatcher, 
            AuditTrailManager auditTrailManager) {
        ArgumentValidator.checkNotNull(settings, "Settings");
        ArgumentValidator.checkNotNull(messageBus, "MessageBus");
        ArgumentValidator.checkNotNull(alarmDispatcher, "AlarmDispatcher");
        ArgumentValidator.checkNotNull(auditTrailManager, "AuditTrailManager");
        
        this.settings = settings;
        this.messageBus = messageBus;
        this.alarmDispatcher = alarmDispatcher;
        this.auditTrailManager = auditTrailManager;
    }
    
    /**
     * @return The settings for this context.
     */
    public Settings getSettings() {
        return settings;
    }
    
    /**
     * @return The message bus for this context.
     */
    public MessageBus getMessageBus() {
        return messageBus;
    }
    
    /**
     * @return The alarm dispatcher for this context.
     */
    public AlarmDispatcher getAlarmDispatcher() {
        return alarmDispatcher;
    }
    
    /**
     * @return The audit trail manger for this context.
     */
    public AuditTrailManager getAuditTrailManager() {
        return auditTrailManager;
    }
}
