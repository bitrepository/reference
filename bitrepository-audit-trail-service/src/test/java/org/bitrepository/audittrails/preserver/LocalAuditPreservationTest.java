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

import org.bitrepository.TestGroups;
import org.bitrepository.audittrails.store.AuditEventIterator;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.DefaultThreadFactory;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.settings.repositorysettings.Collection;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URL;
import java.time.Instant;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LocalAuditPreservationTest {
    /**
     * The settings for the tests. Should be instantiated in the setup.
     */
    Settings settings;

    String PILLAR_ID = "pillarID";
    String collectionID;
    private URL testUploadUrl;
    private DefaultThreadFactory threadFactory;

    @BeforeAll
    void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("LocalAuditPreservationUnderTest");

        Collection c = settings.getRepositorySettings().getCollections().getCollection().get(0);
        settings.getRepositorySettings().getCollections().getCollection().clear();
        settings.getRepositorySettings().getCollections().getCollection().add(c);

        collectionID = c.getID();
        testUploadUrl = new URI("http://TestURL.com").toURL();
        threadFactory = new DefaultThreadFactory(this.getClass().getSimpleName(), Thread.NORM_PRIORITY, false);

    }

    @Test
    // Fragile test, fails occasionally.
    @SuppressWarnings("rawtypes")
    void auditPreservationSchedulingTest() throws Exception {
        addDescription("Tests the scheduling of the audit trail preservation.");
        addStep("Setup variables and settings for the test", "");
        //MockAuditStore store = new MockAuditStore();
        MockPutClient client = new MockPutClient();

        settings.getReferenceSettings().getAuditTrailServiceSettings().setTimerTaskCheckInterval(100);
        Duration interval = DatatypeFactory.newInstance().newDuration(1000);
        settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailPreservation()
                .setAuditTrailPreservationInterval(
                        interval);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs().clear();
        settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs().add(PILLAR_ID);
        SettingsUtils.initialize(settings);

        addStep("Create the preserver", "No calls to store or client");
        FileExchange fileExchangeMock = Mockito.mock(FileExchange.class);
        Mockito.when(fileExchangeMock.getURL(ArgumentMatchers.anyString())).thenReturn(testUploadUrl);
        AuditTrailStore store = Mockito.mock(AuditTrailStore.class);
        Mockito.when(store.getPreservationSequenceNumber(PILLAR_ID, collectionID)).thenReturn(0L);
        final AuditEventIterator iterator = Mockito.spy(new StubAuditEventIterator());

        LocalAuditTrailPreserver preserver = new LocalAuditTrailPreserver(settings, store, client, fileExchangeMock);

        Mockito.verify(store).addCollection(collectionID);
        Mockito.verify(store).addContributor(PILLAR_ID);
        Mockito.verify(store).hasPreservationKey(PILLAR_ID, collectionID);
        Mockito.verify(store).setPreservationSequenceNumber(PILLAR_ID, collectionID, 0L);
        Mockito.verify(store).getPreservationSequenceNumber(PILLAR_ID, collectionID);
        Mockito.verifyNoMoreInteractions(store);

        addStep("Start the preservation scheduling and wait for more than one interval", "");
        Mockito.doAnswer(new Answer() {
            public AuditEventIterator answer(InvocationOnMock invocation) {
                return iterator;
            }
        }).when(store).getAuditTrailsByIterator(ArgumentMatchers.any(), ArgumentMatchers.anyString(),
                ArgumentMatchers.anyString(), ArgumentMatchers.any(Long.class), ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.any(), ArgumentMatchers.isNull(Instant.class),
                ArgumentMatchers.isNull(Instant.class),
                ArgumentMatchers.any(), ArgumentMatchers.any());

        preserver.start();

        synchronized (this) {
            this.wait(500);
        }
        addStep("stop the scheduling", "Should have made calls to the store and the client regarding the preservation");
        preserver.close();
        // getPreservationSequenceNumber should be called twice, first to 'initialize' auditpacker, and second to 
        // run the preserver/packer...
        Mockito.verify(store, Mockito.times(2)).getPreservationSequenceNumber(PILLAR_ID, collectionID);
        Mockito.verify(store).getAuditTrailsByIterator(
                null, collectionID, PILLAR_ID, 1L, null, null,
                null, (Instant) null, (Instant) null, null, null);
        Mockito.verify(iterator, Mockito.times(2)).getNextAuditTrailEvent();
        Assertions.assertEquals(1, client.getCallsToPutFile());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    @SuppressWarnings("rawtypes")
    void auditPreservationIngestTest() throws Exception {
        addDescription("Tests the ingest of the audit trail preservation.");
        addStep("Setup variables and settings for the test", "");
        MockPutClient client = new MockPutClient();

        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(
                PILLAR_ID);
        settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs().clear();
        settings.getRepositorySettings().getGetAuditTrailSettings().getNonPillarContributorIDs().add(PILLAR_ID);
        SettingsUtils.initialize(settings);

        AuditTrailStore store = Mockito.mock(AuditTrailStore.class);

        addStep("Create the preserver and populate the store", "");
        final AuditEventIterator iterator = new StubAuditEventIterator();
        FileExchange fileExchange = Mockito.mock(FileExchange.class);

        LocalAuditTrailPreserver preserver = new LocalAuditTrailPreserver(settings, store, client, fileExchange);

        Mockito.verify(store).addCollection(collectionID);
        Mockito.verify(store).addContributor(PILLAR_ID);
        Mockito.verify(store).getPreservationSequenceNumber(PILLAR_ID, collectionID);
        Mockito.verify(store).hasPreservationKey(PILLAR_ID, collectionID);
        Mockito.verify(store).setPreservationSequenceNumber(PILLAR_ID, collectionID, 0);
        Mockito.verifyNoMoreInteractions(store);

        addStep("Call the preservation of audit trails now.",
                "Should make calls to the store, upload the file and call the client");

        Mockito.when(store.getPreservationSequenceNumber(PILLAR_ID, collectionID)).thenReturn(0L);

        Mockito.doAnswer(invocation -> iterator).when(store).getAuditTrailsByIterator(
                ArgumentMatchers.isNull(),
                ArgumentMatchers.eq(collectionID),
                ArgumentMatchers.eq(PILLAR_ID),
                ArgumentMatchers.eq(1L),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull(Instant.class),
                ArgumentMatchers.isNull(Instant.class),
                ArgumentMatchers.isNull(),
                ArgumentMatchers.isNull());

        Mockito.when(fileExchange.getURL(ArgumentMatchers.anyString())).thenReturn(testUploadUrl);

        preserver.preserveRepositoryAuditTrails();
        // getPreservationSequenceNumber should be called at least twice more: once to 'initialize' audit-packer, and once to
        // run the preserver/packer (on top of the initial call during LocalAuditTrailPreserver construction)
        Mockito.verify(store, Mockito.atLeast(2)).getPreservationSequenceNumber(PILLAR_ID, collectionID);
        Mockito.verify(store).getAuditTrailsByIterator(null, collectionID, PILLAR_ID, 1L,
                null, null, null, (Instant) null, (Instant) null, null,
                null);

        Assertions.assertEquals(1, client.getCallsToPutFile());

        Mockito.verify(fileExchange)
                .putFile(ArgumentMatchers.any(FileInputStream.class), ArgumentMatchers.any(URL.class));
    }

    private class MockPutClient implements PutFileClient {
        private int callsToPutFile = 0;

        @Override
        public void putFile(String collectionID, URL url, String fileID, long sizeOfFile,
                            ChecksumDataForFileTYPE checksumForValidationAtPillar,
                            ChecksumSpecTYPE checksumRequestsForValidation, final EventHandler eventHandler,
                            String auditTrailInformation) {
            callsToPutFile++;
            threadFactory.newThread(() -> {
                try {
                    Thread.sleep(10);
                } catch (InterruptedException ignored) {
                }
                eventHandler.handleEvent(new CompleteEvent(null, null));
            }).start();
        }

        public int getCallsToPutFile() {
            return callsToPutFile;
        }
    }
}
