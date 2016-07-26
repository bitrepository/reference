/*
 * #%L
 * Bitrepository Audit Trail Service
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

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.Date;

import org.bitrepository.access.getaudittrails.AuditTrailClient;
import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
import org.bitrepository.audittrails.collector.AuditTrailCollector;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.ResultingAuditTrails;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.contributor.ContributorMediator;
import org.bitrepository.settings.repositorysettings.Collection;
import org.jaccept.structure.ExtendedTestCase;
import org.mockito.ArgumentCaptor;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AuditTrailServiceTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    
    public static final String TEST_COLLECTION = "dummy-collection";
    public static final String DEFAULT_CONTRIBUTOR = "Contributor1";
 
    
    @BeforeClass (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("AuditTrailServiceUnderTest");
        Collection c = settings.getRepositorySettings().getCollections().getCollection().get(0);
        settings.getRepositorySettings().getCollections().getCollection().clear();
        c.setID(TEST_COLLECTION);
        settings.getRepositorySettings().getCollections().getCollection().add(c);
    }
    
    @Test(groups = {"unstable"})
    public void auditTrailServiceTest() throws Exception {
        addDescription("Test the Audit Trail Service");
        settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs().clear();
        settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs().add(DEFAULT_CONTRIBUTOR);
        settings.getReferenceSettings().getAuditTrailServiceSettings().setCollectAuditInterval(800);
        settings.getReferenceSettings().getAuditTrailServiceSettings().setTimerTaskCheckInterval(100L);
        settings.getReferenceSettings().getAuditTrailServiceSettings().setGracePeriod(800L);

        AuditTrailStore store = mock(AuditTrailStore.class);
        AuditTrailClient client = mock(AuditTrailClient.class);
        AlarmDispatcher alarmDispatcher = mock(AlarmDispatcher.class);

        ContributorMediator mediator = mock(ContributorMediator.class);
        AuditTrailCollector collector = new AuditTrailCollector(settings, client, store, alarmDispatcher);
        
        addStep("Instantiate the service.", "Should work.");
        AuditTrailService service = new AuditTrailService(store, collector, mediator, settings);
        service.start();
        
        addStep("Try to collect audit trails.", "Should make a call to the client.");
        CollectionRunner collectionRunner = new CollectionRunner(service);
        Thread t = new Thread(collectionRunner);
        t.start();
        
        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).getAuditTrails(eq(TEST_COLLECTION), any(AuditTrailQuery[].class),
                isNull(String.class), isNull(String.class), eventHandlerCaptor.capture(), any(String.class));
        
        AuditTrailResult event = new AuditTrailResult(DEFAULT_CONTRIBUTOR, TEST_COLLECTION, new ResultingAuditTrails(), false);
        eventHandlerCaptor.getValue().handleEvent(event);
        eventHandlerCaptor.getValue().handleEvent(new CompleteEvent(TEST_COLLECTION, null));
        
        addStep("Retrieve audit trails with and without an action", "Should work.");
        
        verify(store, times(1)).addAuditTrails(any(AuditTrailEvents.class), eq(TEST_COLLECTION), eq(DEFAULT_CONTRIBUTOR));
        service.queryAuditTrailEventsByIterator(null, null, null, null, null, null, null, null, null);
        verify(store, times(1)).getAuditTrailsByIterator(isNull(String.class), isNull(String.class), 
                isNull(String.class), isNull(Long.class), isNull(Long.class), isNull(String.class), 
                isNull(FileAction.class), isNull(Date.class), isNull(Date.class), isNull(String.class), 
                isNull(String.class));
        service.queryAuditTrailEventsByIterator(null, null, null, null, null, null, FileAction.FAILURE, null, null);
        verify(store, times(1)).getAuditTrailsByIterator(isNull(String.class), isNull(String.class), 
                isNull(String.class), isNull(Long.class), isNull(Long.class), isNull(String.class), 
                eq(FileAction.FAILURE), isNull(Date.class), isNull(Date.class), isNull(String.class), 
                isNull(String.class));

        
        addStep("Shutdown", "");
        service.shutdown();
    }


    public class CollectionRunner implements Runnable {
        private final AuditTrailService service;
        boolean finished = false;

        public CollectionRunner(AuditTrailService service) {
            this.service = service;
        }

        public void run() {
            service.collectAuditTrails();
            finished = true;
        }
    }
}
