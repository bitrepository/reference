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
package org.bitrepository.pillar.checksumpillar;

import javax.jms.JMSException;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.checksumpillar.cache.ChecksumStore;
import org.bitrepository.pillar.checksumpillar.messagehandler.ChecksumPillarMediator;
import org.bitrepository.pillar.common.PillarAlarmDispatcher;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.service.audit.AuditTrailContributerDAO;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.contributor.ContributorContext;
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
    
    /** The cache for persisting the checksum data.*/
    private final ChecksumStore cache;
    /** The mediator for delegating the communication to the message handler.*/
    private ChecksumPillarMediator mediator;
 
    /**
     * Constructor.
     * @param messageBus The message bus for the 
     * @param settings The settings for the checksum pillar.
     * @param refCache The cache for the checksum data to be stored.
     */
    public ChecksumPillar(MessageBus messageBus, Settings settings, ChecksumStore refCache) {
        ArgumentValidator.checkNotNull(messageBus, "messageBus");
        ArgumentValidator.checkNotNull(settings, "settings");
        ArgumentValidator.checkNotNull(refCache, "ChecksumCache refCache");
        
        this.messageBus = messageBus;
        this.cache = refCache;
        
        log.info("Starting the checksum pillar!");
        AuditTrailManager audits = new AuditTrailContributerDAO(settings);
        ContributorContext contributorContext = new ContributorContext(messageBus, settings, 
                settings.getReferenceSettings().getPillarSettings().getPillarID(), 
                settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
        PillarAlarmDispatcher alarms = new PillarAlarmDispatcher(contributorContext);
        PillarContext context = new PillarContext(settings, messageBus, alarms, audits);
        mediator = new ChecksumPillarMediator(context, cache);
        mediator.start();
        log.info("ReferencePillar started!");
    }
    
    /**
     * Close the pillar, and thus also the mediator and the connection to the messagebus.
     */
    public void close() {
        try {
            mediator.close();
            messageBus.close();
        } catch (JMSException e) {
            log.warn("Could not close the messagebus.", e);
        }
    }
}
