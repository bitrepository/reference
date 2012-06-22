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

import org.bitrepository.audittrails.MockAuditStore;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;

public class LocalAuditPreservationTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    
    String PILLARID = "pillarId";
    String ACTOR = "actor";
    
    @BeforeClass (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("LocalAuditPreservationUnderßTest");
    }
    
    @Test(groups = {"regressiontest"})
    public void auditPreservationSchedulingTest() throws Exception {
        addDescription("Tests the scheduling of the audit trail preservation.");
        addStep("Setup variables and settings for the test", "");
        MockAuditStore store = new MockAuditStore();
        MockPutClient client = new MockPutClient();
        
        settings.getReferenceSettings().getAuditTrailServiceSettings().setTimerTaskCheckInterval(100);
        settings.getReferenceSettings().getAuditTrailServiceSettings().setAuditTrailPreservationInterval(300);
        settings.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs().clear();
        settings.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs().add(PILLARID);
        
        addStep("Create the preserver", "No calls to store or client");
        LocalAuditTrailPreserver preserver = new LocalAuditTrailPreserver(settings, store, client);
        
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 0);
        Assert.assertEquals(store.getCallsToGetAuditTrails(), 0);
        Assert.assertEquals(store.getCallsToGetPreservationSequenceNumber(), 1);
        Assert.assertEquals(store.getCallsToLargestSequenceNumber(), 0);
        Assert.assertEquals(store.getCallsToSetPreservationSequenceNumber(), 0);
        Assert.assertEquals(client.getCallsToPutFile(), 0);
        
        addStep("Start the preservation scheduling and wait for more than one interval", "");
        preserver.start();
        
        synchronized(this) {
            this.wait(500);
        }
        
        addStep("stop the scheduling", "Should have made calls to the store and the client regarding the preservation");
        preserver.close();
        Assert.assertEquals(store.getCallsToGetAuditTrails(), settings.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs().size());
        Assert.assertEquals(store.getCallsToGetPreservationSequenceNumber(), 2);
        Assert.assertEquals(client.getCallsToPutFile(), 1);
    }
    
    @SuppressWarnings({ "deprecation" })
    @Test(groups = {"regressiontest"})
    public void auditPreservationIngestTest() throws Exception {
        addDescription("Tests the ingest of the audit trail preservation.");
        addStep("Setup variables and settings for the test", "");
        MockAuditStore store = new MockAuditStore();
        MockPutClient client = new MockPutClient();
        
        settings.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs().clear();
        settings.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs().add(PILLARID);
        
        addStep("Create the preserver and populate the store", "");
        store.addAuditTrails(createEvents());
        LocalAuditTrailPreserver preserver = new LocalAuditTrailPreserver(settings, store, client);
        Assert.assertEquals(store.getCallsToGetAuditTrails(), 0);
        Assert.assertEquals(store.getCallsToGetPreservationSequenceNumber(), 1);
        Assert.assertEquals(client.getCallsToPutFile(), 0);
        
        addStep("Call the preservation of audit trails now.", 
                "Should make calls to the store, upload the file and call the client");
        preserver.preserveAuditTrailsNow();
        
        Assert.assertEquals(store.getCallsToGetAuditTrails(), settings.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs().size());
        Assert.assertEquals(store.getCallsToGetPreservationSequenceNumber(), 2);
        Assert.assertEquals(client.getCallsToPutFile(), 1);
        
        addStep("Check whether a file has been uploaded.", "");
        URL url = client.getUrl();
        FileExchange fileExchange = ProtocolComponentFactory.getInstance().getFileExchange();
        InputStream is = fileExchange.downloadFromServer(url);
        
        Assert.assertTrue(is.read() != -1, "Should be able to read content from the URL.");
        is.close();
    }
    
    private AuditTrailEvents createEvents() {
        AuditTrailEvents res = new AuditTrailEvents();
        
        AuditTrailEvent e1 = new AuditTrailEvent();
        e1.setActionDateTime(CalendarUtils.getNow());
        e1.setActionOnFile(FileAction.FAILURE);
        e1.setActorOnFile(ACTOR);
        e1.setSequenceNumber(BigInteger.ONE);
        e1.setReportingComponent(PILLARID);
        
        res.getAuditTrailEvent().add(e1);
        return res;
    }

    private class MockPutClient implements PutFileClient {
        @Override
        public void shutdown() { }

        private URL url = null;
        public URL getUrl() {
            return url;
        }
        private int callsToPutFile = 0;
        @Override
        public void putFile(URL url, String fileId, long sizeOfFile,
                ChecksumDataForFileTYPE checksumForValidationAtPillar, ChecksumSpecTYPE checksumRequestsForValidation,
                EventHandler eventHandler, String auditTrailInformation) {
            this.url = url;
            callsToPutFile++;
        }
        public int getCallsToPutFile() {
            return callsToPutFile;
        }
        
    }
}
