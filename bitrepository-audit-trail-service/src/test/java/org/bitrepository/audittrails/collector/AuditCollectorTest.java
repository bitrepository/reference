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
package org.bitrepository.audittrails.collector;

import org.bitrepository.access.getaudittrails.AuditTrailClient;
import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.bitrepositoryelements.ResultingAuditTrails;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.settings.repositorysettings.Collection;
import org.jaccept.structure.ExtendedTestCase;
import org.mockito.ArgumentCaptor;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

public class AuditCollectorTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    
    public static final String TEST_COLLECTION = "dummy-collection";
    public static final String DEFAULT_CONTRIBUTOR = "Contributor1";
    
    @BeforeClass (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("AuditCollectorUnderTest");
        Collection c = settings.getRepositorySettings().getCollections().getCollection().get(0);
        c.setID(TEST_COLLECTION);
        settings.getRepositorySettings().getCollections().getCollection().clear();
        settings.getRepositorySettings().getCollections().getCollection().add(c);
    }

    @Test(groups = {"regressiontest"})
    public void auditCollectorIntervalTest() throws Exception {
        addDescription("Test that the collector calls the AuditClient at the correct intervals.");
        settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs().clear();
        settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs().add(DEFAULT_CONTRIBUTOR);
        settings.getReferenceSettings().getAuditTrailServiceSettings().setCollectAuditInterval(800);
        settings.getReferenceSettings().getAuditTrailServiceSettings().setTimerTaskCheckInterval(100L);
        settings.getReferenceSettings().getAuditTrailServiceSettings().setGracePeriod(800L);

        SettingsUtils.initialize(settings);
        AuditTrailClient client = mock(AuditTrailClient.class);
        AuditTrailStore store = mock(AuditTrailStore.class);
        AlarmDispatcher alarmDispatcher = mock(AlarmDispatcher.class);
        AuditTrailCollector collector = new AuditTrailCollector(settings, client, store, alarmDispatcher);
        
        ArgumentCaptor<EventHandler> eventHandlerCaptor = ArgumentCaptor.forClass(EventHandler.class);
        verify(client, timeout(3000).times(1)).getAuditTrails(eq(TEST_COLLECTION), any(AuditTrailQuery[].class),
                isNull(), isNull(), eventHandlerCaptor.capture(), any(String.class));
        EventHandler eventHandler = eventHandlerCaptor.getValue(); 
        
        Assert.assertNotNull(eventHandler, "Should have an event handler");
        eventHandler.handleEvent(new AuditTrailResult(DEFAULT_CONTRIBUTOR, TEST_COLLECTION, new ResultingAuditTrails(), false));
        eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
        
        verify(client, timeout(3000).times(2)).getAuditTrails(eq(TEST_COLLECTION), any(AuditTrailQuery[].class),
                isNull(), isNull(), eventHandlerCaptor.capture(), any(String.class));
        eventHandler = eventHandlerCaptor.getValue();
        eventHandler.handleEvent(new AuditTrailResult(DEFAULT_CONTRIBUTOR, TEST_COLLECTION, new ResultingAuditTrails(), false));
        eventHandler.handleEvent(new CompleteEvent(TEST_COLLECTION, null));
        
        collector.close();
    }
}
