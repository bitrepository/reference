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

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.common.utils.XmlUtils;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.messagehandler.PillarMediator;
import org.bitrepository.pillar.schedulablejobs.RecalculateChecksumJob;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.service.scheduler.JobScheduler;
import org.bitrepository.service.scheduler.TimerBasedScheduler;
import org.bitrepository.service.workflow.SchedulableJob;
import org.bitrepository.settings.referencesettings.PillarType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.xml.datatype.Duration;

/**
 * Class for the Reference Pillar.
 * This will either be a File Reference Pillar or a Checksum Reference Pillar, depending on the type of
 * storage model.
 */
public class Pillar {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final MessageBus messageBus;
    private final Settings settings;
    private final StorageModel pillarModel;
    private final PillarMediator mediator;
    private final JobScheduler scheduler;
    /**
     * The default time for running the recalculation workflow, when the settings are not set.
     * The default is every hour.
     */
    private static final Long DEFAULT_RECALCULATION_WORKFLOW_TIME = 3600000L;

    /**
     * @param messageBus  The message-bus for the communication.
     * @param settings    The settings for the pillar.
     * @param pillarModel The storage model for the pillar.
     * @param context     The context for the message handler.
     */
    public Pillar(MessageBus messageBus, Settings settings, StorageModel pillarModel, MessageHandlerContext context) {
        ArgumentValidator.checkNotNull(messageBus, "messageBus");
        ArgumentValidator.checkNotNull(settings, "settings");
        this.messageBus = messageBus;
        SettingsUtils.initialize(settings);
        this.settings = settings;
        this.pillarModel = pillarModel;

        PillarType pillarType = settings.getReferenceSettings().getPillarSettings().getPillarType();

        log.info("Starting the ReferencePillar of type '{}'", pillarType);
        messageBus.setCollectionFilter(context.getPillarCollections());
        mediator = new PillarMediator(messageBus, context, pillarModel);
        mediator.start();

        this.scheduler = new TimerBasedScheduler();
        if (pillarType == PillarType.FILE) {
            initializeWorkflows();
        }
    }

    /**
     * Initializes one RecalculateChecksums workflow for each collection.
     */
    private void initializeWorkflows() {
        Long interval = DEFAULT_RECALCULATION_WORKFLOW_TIME;
        Duration recalculateOldChecksumsInterval = settings.getReferenceSettings().getPillarSettings()
                .getRecalculateOldChecksumsInterval();
        if (recalculateOldChecksumsInterval != null) {
            interval = XmlUtils.xmlDurationToMilliseconds(recalculateOldChecksumsInterval);
        }
        for (String collectionID : SettingsUtils.getCollectionIDsForPillar(
                settings.getReferenceSettings().getPillarSettings().getPillarID())) {
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
            log.warn("Could not close the message-bus.", e);
        }
    }
}
