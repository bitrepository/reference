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
package org.bitrepository.audittrails.preserver;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.testng.Assert.assertEquals;

import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.URL;
import java.sql.Date;

import org.bitrepository.audittrails.store.AuditEventIterator;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.settings.repositorysettings.Collection;
import org.jaccept.structure.ExtendedTestCase;
import org.mockito.ArgumentCaptor;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class LocalAuditPreservationTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    
    String PILLARID = "pillarId";
    String ACTOR = "actor";
    String collectionId;
    private URL testUploadUrl;

    
    @BeforeClass (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("LocalAuditPreservationUnderTest");
        
        Collection c = settings.getRepositorySettings().getCollections().getCollection().get(0);
        settings.getRepositorySettings().getCollections().getCollection().clear();
        settings.getRepositorySettings().getCollections().getCollection().add(c);
        
        collectionId = c.getID();
        testUploadUrl = new URL("http://TestURL.com");
    }


    //@Test(groups = {"failed"})
    // Fragile test, fails occasionally.
    @SuppressWarnings("rawtypes")
    public void auditPreservationSchedulingTest() throws Exception {
        addDescription("Tests the scheduling of the audit trail preservation.");
        addStep("Setup variables and settings for the test", "");
        //MockAuditStore store = new MockAuditStore();
        MockPutClient client = new MockPutClient();
        
        settings.getReferenceSettings().getAuditTrailServiceSettings().setTimerTaskCheckInterval(100);
        settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailPreservation().setAuditTrailPreservationInterval(300);
        settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs().clear();
        settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs().add(PILLARID);
        
        addStep("Create the preserver", "No calls to store or client");
        FileExchange fileExchangeMock = mock(FileExchange.class);
        AuditTrailStore store = mock(AuditTrailStore.class);
        final AuditEventIterator iterator = mock(AuditEventIterator.class);
        
        LocalAuditTrailPreserver preserver = new LocalAuditTrailPreserver(settings, store, client, fileExchangeMock);
        
        /*Assert.assertEquals(store.getCallsToAddAuditTrails(), 0);
        Assert.assertEquals(store.getCallsToGetAuditTrails(), 0);
        Assert.assertEquals(store.getCallsToGetPreservationSequenceNumber(), 1);
        Assert.assertEquals(store.getCallsToLargestSequenceNumber(), 0);
        Assert.assertEquals(store.getCallsToSetPreservationSequenceNumber(), 0);
        Assert.assertEquals(client.getCallsToPutFile(), 0);*/
        
        verify(store).getPreservationSequenceNumber(PILLARID, collectionId);
        verifyNoMoreInteractions(store);
        
        addStep("Start the preservation scheduling and wait for more than one interval", "");
        doAnswer(new Answer() {
            public AuditEventIterator answer(InvocationOnMock invocation) {
                return iterator;
            }
        }).when(store).getAuditTrailsByIterator(anyString(), anyString(), anyString(), any(Long.class), 
                any(Long.class), anyString(), any(FileAction.class), any(Date.class), any(Date.class), 
                anyString(), anyString());
        
        preserver.start();
        
        synchronized(this) {
            this.wait(500);
        }
        verifyNoMoreInteractions(store);
        addStep("stop the scheduling", "Should have made calls to the store and the client regarding the preservation");
        preserver.close();
        // getPreservationSequenceNumber should be called twice, first to 'initialize' auditpacker, and second to 
        // run the preserver/packer...
        verify(store, times(2)).getPreservationSequenceNumber(PILLARID, collectionId);
        verify(store).getAuditTrailsByIterator(null, null, PILLARID, 0L, 
                null, null, null, null, null, null, null);
        verify(iterator).getNextAuditTrailEvent();
        //Assert.assertEquals(store.getCallsToGetAuditTrails(), settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs().size());
        
        //Assert.assertEquals(store.getCallsToGetPreservationSequenceNumber(), 2);
        assertEquals(client.getCallsToPutFile(), 1);
    }

    @Test(groups = {"regressiontest"})
    @SuppressWarnings("rawtypes")
    public void auditPreservationIngestTest() throws Exception {
        addDescription("Tests the ingest of the audit trail preservation.");
        addStep("Setup variables and settings for the test", "");
        MockPutClient client = new MockPutClient();

        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLARID);
        settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs().clear();
        settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs().add(PILLARID);
        SettingsUtils.initialize(settings);

        AuditTrailStore store = mock(AuditTrailStore.class);
        
        addStep("Create the preserver and populate the store", "");
        final AuditEventIterator iterator = new StubAuditEventIterator();
        FileExchange fileExchange = mock(FileExchange.class);

        LocalAuditTrailPreserver preserver = new LocalAuditTrailPreserver(settings, store, client, fileExchange);
        
        verify(store).getPreservationSequenceNumber(PILLARID, collectionId);
        verifyNoMoreInteractions(store);
        
        addStep("Call the preservation of audit trails now.", 
                "Should make calls to the store, upload the file and call the client");

        doAnswer(invocation -> iterator).when(store).getAuditTrailsByIterator(
                anyString(), anyString(), anyString(), any(Long.class), any(Long.class), anyString(),
                any(FileAction.class), any(Date.class), any(Date.class), anyString(), anyString());
        
        doAnswer(invocation -> testUploadUrl).when(fileExchange).uploadToServer(any(FileInputStream.class), anyString());
                
        preserver.preserveRepositoryAuditTrails();
        // getPreservationSequenceNumber should be called twice, first to 'initialize' auditpacker, and second to
        // run the preserver/packer...
        verify(store, times(2)).getPreservationSequenceNumber(PILLARID, collectionId);
        verify(store).getAuditTrailsByIterator(null, collectionId, PILLARID, 0L,
                null, null, null, null, null, null, null);

        Assert.assertEquals(client.getCallsToPutFile(), 1);

        verify(fileExchange).uploadToServer(any(FileInputStream.class), anyString());
        verifyNoMoreInteractions(fileExchange);
    }
    
    private class MockPutClient implements PutFileClient {
        private int callsToPutFile = 0;
        @Override
        public void putFile(String collectionID, URL url, String fileId, long sizeOfFile,
                ChecksumDataForFileTYPE checksumForValidationAtPillar, ChecksumSpecTYPE checksumRequestsForValidation,
                final EventHandler eventHandler, String auditTrailInformation) {
            callsToPutFile++;
            new Thread(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {}
                eventHandler.handleEvent(new CompleteEvent(null, null));
            }).start();
        }
        public int getCallsToPutFile() {
            return callsToPutFile;
        }
    }
    
    private class StubAuditEventIterator extends AuditEventIterator {
        boolean called = false;
        public StubAuditEventIterator() {
            super(null);
        }
        
        @Override
        public AuditTrailEvent getNextAuditTrailEvent() {
            if(called) {
                return null;
            } else {
                called = true;
                AuditTrailEvent e1 = new AuditTrailEvent();
                e1.setActionDateTime(CalendarUtils.getNow());
                e1.setActionOnFile(FileAction.FAILURE);
                e1.setActorOnFile(ACTOR);
                e1.setSequenceNumber(BigInteger.ONE);
                e1.setReportingComponent(PILLARID);
                return e1;
            }
        }
    }
}
