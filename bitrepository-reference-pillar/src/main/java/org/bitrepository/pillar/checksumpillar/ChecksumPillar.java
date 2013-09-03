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

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.pillar.Pillar;
import org.bitrepository.pillar.cache.ChecksumStore;
import org.bitrepository.pillar.checksumpillar.messagehandler.ChecksumPillarMediator;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.common.PillarAlarmDispatcher;
import org.bitrepository.pillar.common.SettingsHelper;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.service.audit.AuditDatabaseManager;
import org.bitrepository.service.audit.AuditTrailContributerDAO;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.contributor.ResponseDispatcher;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;

import java.util.Arrays;

/**
 * The checksum pillar.
 */
public class ChecksumPillar implements Pillar {
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

        SettingsUtils.initialize(settings);
        this.messageBus = messageBus;
        this.cache = refCache;
        
        log.info("Starting the ChecksumPillar");
        DatabaseManager auditDatabaseManager = new AuditDatabaseManager(
                settings.getReferenceSettings().getPillarSettings().getAuditTrailContributerDatabase());
        AuditTrailManager audits = new AuditTrailContributerDAO(settings, auditDatabaseManager);
        MessageHandlerContext context = new MessageHandlerContext(settings,
                SettingsHelper.getPillarCollections(settings.getComponentID(), settings.getCollections()),
            new ResponseDispatcher(settings, messageBus),
            new PillarAlarmDispatcher(settings, messageBus),
            audits);
        messageBus.setCollectionFilter(Arrays.asList(context.getPillarCollections()));
        mediator = new ChecksumPillarMediator(messageBus, context, cache);
        mediator.start();
        log.info("ChecksumPillar started!");
    }
    
    /**
     * Close the pillar, and thus also the mediator and the connection to the messagebus.
     */
    public void close() {
        try {
            mediator.close();
            messageBus.close();
            log.info("ChecksumPillar stopped");
        } catch (JMSException e) {
            log.warn("Could not close the message bus properly.", e);
        }
    }
}
