/*
 * #%L
 * Bitrepository Audit Trail Service
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.audittrails;

import java.util.Collection;
import java.util.Date;
import javax.jms.JMSException;
import org.bitrepository.audittrails.collector.AuditTrailCollector;
import org.bitrepository.audittrails.preserver.AuditTrailPreserver;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.service.LifeCycledService;
import org.bitrepository.service.contributor.ContributorMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to expose the functionality of the AuditTrailService. 
 * Aggregates the needed classes.   
 */
public class AuditTrailService implements LifeCycledService {
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The storage of audit trail information.*/
    private final AuditTrailStore store;
    /** The collector of new audit trails.*/
    private final AuditTrailCollector collector;
    /** The mediator for handling the messages.*/
    private final ContributorMediator mediator;
    /** The preserver of audit trails.*/
    private final AuditTrailPreserver preserver;
    /** The AuditTrailService settings.*/
    private final Settings settings;

    /**
     * Constructor.
     * @param store The store for the audit trail data.
     * @param collector The collector of new audit trail data.
     * @param mediator The mediator for the communication of this contributor.
     * @param preserver Instance for handling the preservation of audit trails.
     * @param settings The AuditTrailService settings.
     */
    public AuditTrailService(
            AuditTrailStore store,
            AuditTrailCollector collector,
            ContributorMediator mediator,
            AuditTrailPreserver preserver,
            Settings settings) {
        ArgumentValidator.checkNotNull(collector, "AuditTrailCollector collector");
        ArgumentValidator.checkNotNull(store, "AuditTrailStore store");
        ArgumentValidator.checkNotNull(mediator, "ContributorMediator mediator");
        ArgumentValidator.checkNotNull(preserver, "AuditTrailPreserver preserver");

        this.store = store;
        this.collector = collector;
        this.mediator = mediator;
        this.preserver = preserver;
        this.settings = settings;

        mediator.start();
        preserver.start();
    }

    /**
     * Retrieve all AuditTrailEvents matching the criteria from the parameters.
     * All parameters are allowed to be null, meaning that the parameter imposes no restriction on the result
     * @param fromDate Restrict the results to only provide events after this point in time
     * @param toDate Restrict the results to only provide events up till this point in time
     * @param fileID Restrict the results to only be about this fileID
     * @param reportingComponent Restrict the results to only be reported by this component
     * @param actor Restrict the results to only be events caused by this actor
     * @param action Restrict the results to only be about this type of action
     */
    public Collection<AuditTrailEvent> queryAuditTrailEvents(Date fromDate, Date toDate, String fileID, 
            String collectionID, String reportingComponent, String actor, FileAction action, Integer maxResults) {
        return store.getAuditTrails(fileID, collectionID, reportingComponent, null, null, actor, action, 
                fromDate, toDate, maxResults);
    }

    /**
     * Collects all the newest audit trails.
     */
    public void collectAuditTrails() {
        collector.collectNewestAudits();
    }

    /**
     * Performs the preservation of audit trails.
     */
    public void preserveAuditTrailsNow() {
        preserver.preserveAuditTrailsNow();
    }

    @Override
    public void start() {
        mediator.start();
    }

    @Override
    public void shutdown() {
        collector.close();
        store.close();
        mediator.close();
        MessageBus messageBus = MessageBusManager.getMessageBus();
        if ( messageBus != null) {
            try {
                messageBus.close();
            } catch (JMSException e) {
                log.warn("Failed to close message bus cleanly, " + e.getMessage());
            }
        }
    }
}
