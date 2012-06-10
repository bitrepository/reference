/*
 * #%L
 * Bitrepository Reference Pillar
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.pillar.common;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.contributor.ContributorContext;

/**
 * Container for the context of the pillar, e.g. all the components needed for the message handling.
 */
public class PillarContext {
    /** The message bus.*/
    private final MessageBus messageBus;
    /** The alarm dispatcher.*/
    private final PillarAlarmDispatcher alarmDispatcher;
    /** The audit trail manager.*/
    private final AuditTrailManager auditTrailManager;
    /** The mediator context.*/
    private final ContributorContext mediatorContext;
    
    /**
     * Constructor.
     * @param settings The settings.
     * @param messageBus The message bus.
     * @param alarmDispatcher The alarm dispatcher.
     * @param auditTrailManager The audit trial manager.
     */
    public PillarContext(Settings settings, MessageBus messageBus, PillarAlarmDispatcher alarmDispatcher, 
            AuditTrailManager auditTrailManager) {
        ArgumentValidator.checkNotNull(settings, "settings");
        ArgumentValidator.checkNotNull(messageBus, "messageBus");
        ArgumentValidator.checkNotNull(alarmDispatcher, "AlarmDispatcher");
        ArgumentValidator.checkNotNull(auditTrailManager, "AuditTrailManager");
        
        mediatorContext = new ContributorContext(messageBus, settings);
        this.messageBus = messageBus;
        this.alarmDispatcher = alarmDispatcher;
        this.auditTrailManager = auditTrailManager;
    }
    
    /**
     * @return The mediator context.
     */
    public ContributorContext getMediatorContext() {
        return mediatorContext;
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
    public PillarAlarmDispatcher getAlarmDispatcher() {
        return alarmDispatcher;
    }
    
    /**
     * @return The audit trail manger for this context.
     */
    public AuditTrailManager getAuditTrailManager() {
        return auditTrailManager;
    }
    
    /**
     * @return The settings for this context.
     */
    public Settings getSettings() {
        return mediatorContext.getSettings();
    }
}
