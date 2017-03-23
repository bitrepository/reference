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
package org.bitrepository.pillar;

import javax.jms.JMSException;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.common.PillarAlarmDispatcher;
import org.bitrepository.pillar.common.SettingsHelper;
import org.bitrepository.pillar.messagehandler.PillarMediator;
import org.bitrepository.pillar.schedulablejobs.RecalculateChecksumJob;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumStore;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.contributor.ResponseDispatcher;
import org.bitrepository.service.scheduler.JobScheduler;
import org.bitrepository.service.scheduler.TimerbasedScheduler;
import org.bitrepository.service.workflow.SchedulableJob;
import org.bitrepository.settings.referencesettings.PillarType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bitrepository.pillar.PillarComponentFactory.getAuditTrailManager;
import static org.bitrepository.pillar.PillarComponentFactory.getChecksumStore;
import static org.bitrepository.pillar.PillarComponentFactory.getPillarModel;

/**
 * Class for the Reference Pillar.
 * This will either be a File Reference Pillar or a Checksum Reference Pillar, depending on the type of
 * storage model.
 */
public class Pillar {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The messagebus for the pillar.*/
    private final MessageBus messageBus;
    /** The settings.*/
    private final Settings settings;
    /** The storage model for all the file data.*/
    private final StorageModel pillarModel;

    private final PillarMediator mediator;
    
    /** The scheduler for the recalculation workflows.*/
    private final JobScheduler scheduler;
    /** The default time for running the recalculation workflow, when the settings is not set.
     * The default is every hour. */
    private static final Long DEFAULT_RECALCULATION_WORKFLOW_TIME = 3600000L;

    /**
     * Constructor.
     *  @param settings    The settings for the pillar.
     * @param securityManager
     */
    public Pillar(Settings settings, SecurityManager securityManager) {
        this.settings = settings;

        messageBus = MessageBusManager.createMessageBus(settings, securityManager);

        PillarAlarmDispatcher alarmDispatcher = new PillarAlarmDispatcher(settings, messageBus);

        ChecksumStore cache = getChecksumStore(settings);
        pillarModel = getPillarModel(settings, cache, alarmDispatcher);

        FileExchange fileExchange = ProtocolComponentFactory.createFileExchange(settings);

        ResponseDispatcher responseDispatcher = new ResponseDispatcher(settings, messageBus);
        AuditTrailManager audits = getAuditTrailManager(settings);
        MessageHandlerContext context = new MessageHandlerContext(
                settings,
                SettingsHelper.getPillarCollections(settings.getComponentID(), settings.getCollections()),
                responseDispatcher,
                alarmDispatcher,
                audits,
                fileExchange);

        SettingsUtils.initialize(settings);
        /* The type of pillar.*/
        PillarType pillarType = settings.getReferenceSettings().getPillarSettings().getPillarType();
        log.info("Starting the ReferencePillar of type '" + pillarType + "'.");

        messageBus.setCollectionFilter(context.getPillarCollections());
        mediator = new PillarMediator(messageBus, context, pillarModel);
        mediator.start();
        
        this.scheduler = new TimerbasedScheduler();
        if(pillarType == PillarType.FILE) {
            initializeWorkflows();
        }
    }
    
    /**
     * Initializes one RecalculateChecksums workflow for each collection.
     */
    private void initializeWorkflows() {
        Long interval = DEFAULT_RECALCULATION_WORKFLOW_TIME;
        if(settings.getReferenceSettings().getPillarSettings().getRecalculateOldChecksumsInterval() != null) {
            interval = settings.getReferenceSettings().getPillarSettings()
                    .getRecalculateOldChecksumsInterval().longValue();
        }
        for(String collectionID : SettingsUtils.getCollectionIDsForPillar(
                settings.getReferenceSettings().getPillarSettings().getPillarID(), settings)) {
            SchedulableJob workflow = new RecalculateChecksumJob(collectionID, pillarModel);
            scheduler.schedule(workflow, interval);
        }
    }
    
    /**
     * Closes the ReferencePillar.
     */
    public void close() {
        try {
            mediator.close();
            messageBus.close();
            pillarModel.close();
            log.info("ReferencePillar stopped!");
        } catch (JMSException e) {
            log.warn("Could not close the messagebus.", e);
        }
    }
}
