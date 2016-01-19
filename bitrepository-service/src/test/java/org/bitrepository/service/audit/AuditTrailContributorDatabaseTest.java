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
package org.bitrepository.service.audit;

import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_ACTOR_GUID;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_AUDIT;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_FILE_GUID;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_FINGERPRINT;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_INFORMATION;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_OPERATION;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_OPERATIONID;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_OPERATION_DATE;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_TABLE;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_SEQUENCE_NUMBER;

import java.util.Date;

import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.service.database.DatabaseCreator;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.service.database.DatabaseUtils;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/** Run audit trail contributor database test using Derby.  Generates jaccept reports. */

public class AuditTrailContributorDatabaseTest extends ExtendedTestCase {
    private Settings settings;
    private DatabaseSpecifics databaseSpecifics;
    private String firstCollectionID;
    
    private static final String DEFAULT_ACTOR = "ACTOR";
    private static final String DEFAULT_INFO = "Adding a info";
    private static final String DEFAULT_AUDIT_TRAIL_MESSAGE = "AuditTrail";
    private static final String DEFAULT_OPERATION_ID = "op1";
    private static final String DEFAULT_CERTIFICATE_ID = "aa";
    private static final String FILE_ID_1 = "FILE-ID-1";
    private static final String FILE_ID_2 = "FILE-ID-2";

    @BeforeClass (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings(getClass().getSimpleName());

        databaseSpecifics = new DatabaseSpecifics();
        databaseSpecifics.setDriverClass("org.apache.derby.jdbc.EmbeddedDriver");
        databaseSpecifics.setDatabaseURL("jdbc:derby:target/test/auditcontributerdb");

        DerbyDatabaseDestroyer.deleteDatabase(databaseSpecifics);

        TestAuditTrailContributorDBCreator dbCreator = new TestAuditTrailContributorDBCreator();
        dbCreator.createAuditTrailContributorDatabase(databaseSpecifics);
        firstCollectionID = settings.getCollections().get(0).getID();
    }

    @Test(groups = {"regressiontest", "databasetest"})
    public void testAuditTrailDatabaseExtraction() throws Exception {
        addDescription("Testing the basic functions of the audit trail database interface.");
        addStep("Setup varibles and the database connection.", "No errors.");
        DatabaseManager dm = new AuditDatabaseManager(databaseSpecifics);
        AuditTrailContributerDAO daba = new DerbyAuditTrailContributorDAO(dm);
        daba.initialize(settings.getComponentID());

        addStep("Populate the database.", "Should be inserted into database.");
        daba.addAuditEvent(firstCollectionID, FILE_ID_1, DEFAULT_ACTOR, DEFAULT_INFO, DEFAULT_AUDIT_TRAIL_MESSAGE, FileAction.PUT_FILE, DEFAULT_OPERATION_ID, DEFAULT_CERTIFICATE_ID);
        daba.addAuditEvent(firstCollectionID, FILE_ID_1, DEFAULT_ACTOR, DEFAULT_INFO, DEFAULT_AUDIT_TRAIL_MESSAGE, FileAction.CHECKSUM_CALCULATED, DEFAULT_OPERATION_ID, DEFAULT_CERTIFICATE_ID);
        daba.addAuditEvent(firstCollectionID, FILE_ID_2, DEFAULT_ACTOR, DEFAULT_INFO, DEFAULT_AUDIT_TRAIL_MESSAGE, FileAction.FILE_MOVED, DEFAULT_OPERATION_ID, DEFAULT_CERTIFICATE_ID);
        daba.addAuditEvent(firstCollectionID, FILE_ID_2, DEFAULT_ACTOR, DEFAULT_INFO, DEFAULT_AUDIT_TRAIL_MESSAGE, FileAction.FAILURE, DEFAULT_OPERATION_ID, DEFAULT_CERTIFICATE_ID);
        daba.addAuditEvent(firstCollectionID, FILE_ID_2, DEFAULT_ACTOR, DEFAULT_INFO, DEFAULT_AUDIT_TRAIL_MESSAGE, FileAction.INCONSISTENCY, DEFAULT_OPERATION_ID, DEFAULT_CERTIFICATE_ID);
        
        addStep("Test extracting all the events", "Should be all 5 events.");
        AuditTrailDatabaseResults events = daba.getAudits(firstCollectionID, null, null, null, null, null, null);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 5);
        
        addStep("Test extracting the events for fileID1", "Should be 2 events.");
        events = daba.getAudits(firstCollectionID, FILE_ID_1, null, null, null, null, null);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 2);

        addStep("Test extracting the events for fileID2", "Should be 3 events.");
        events = daba.getAudits(firstCollectionID, FILE_ID_2, null, null, null, null, null);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 3);
        
        addStep("Test extracting the events with the sequence number at least equal to the largest sequence number.", 
                "Should be 1 event.");
        Long seq = daba.extractLargestSequenceNumber();
        events = daba.getAudits(firstCollectionID, null, seq, null, null, null, null);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 1);
        
        addStep("Test extracting the events for fileID1 with sequence number 2 or more", "Should be 1 event.");
        events = daba.getAudits(firstCollectionID, FILE_ID_1, seq-3, null, null, null, null);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 1);

        addStep("Test extracting the events for fileID1 with at most sequence number 2", "Should be 2 events.");
        events = daba.getAudits(firstCollectionID, FILE_ID_1, null, seq-3, null, null, null);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 2);

        addStep("Test extracting at most 3 events", "Should extract 3 events.");
        events = daba.getAudits(firstCollectionID, null, null, null, null, null, 3L);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 3);

        addStep("Test extracting at most 1000 events", "Should extract all 5 events.");
        events = daba.getAudits(firstCollectionID, null, null, null, null, null, 1000L);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 5);
        
        addStep("Test extracting from another collection", "Should not extract anything.");
        String secondCollectionID = settings.getCollections().get(1).getID();
        events = daba.getAudits(secondCollectionID, null, null, null, null, null, 1000L);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 0);
        
        dm.getConnector().destroy();
    }
    
    @Test(groups = {"regressiontest", "databasetest"})
    public void testAuditTrailDatabaseExtractionOrder() throws Exception {
        addDescription("Test the order of extraction");
        addStep("Setup variables and database connection", "No errors");
        DatabaseManager dm = new AuditDatabaseManager(databaseSpecifics);
        AuditTrailContributerDAO daba = new DerbyAuditTrailContributorDAO(dm);
        daba.initialize(settings.getComponentID());

        addStep("Populate the database.", "Should be inserted into database.");
        daba.addAuditEvent(firstCollectionID, FILE_ID_1, DEFAULT_ACTOR, DEFAULT_INFO, DEFAULT_AUDIT_TRAIL_MESSAGE, FileAction.PUT_FILE, DEFAULT_OPERATION_ID, DEFAULT_CERTIFICATE_ID);
        daba.addAuditEvent(firstCollectionID, FILE_ID_1, DEFAULT_ACTOR, DEFAULT_INFO, DEFAULT_AUDIT_TRAIL_MESSAGE, FileAction.CHECKSUM_CALCULATED, DEFAULT_OPERATION_ID, DEFAULT_CERTIFICATE_ID);
        daba.addAuditEvent(firstCollectionID, FILE_ID_2, DEFAULT_ACTOR, DEFAULT_INFO, DEFAULT_AUDIT_TRAIL_MESSAGE, FileAction.FILE_MOVED, DEFAULT_OPERATION_ID, DEFAULT_CERTIFICATE_ID);
        daba.addAuditEvent(firstCollectionID, FILE_ID_2, DEFAULT_ACTOR, DEFAULT_INFO, DEFAULT_AUDIT_TRAIL_MESSAGE, FileAction.FAILURE, DEFAULT_OPERATION_ID, DEFAULT_CERTIFICATE_ID);
        daba.addAuditEvent(firstCollectionID, FILE_ID_2, DEFAULT_ACTOR, DEFAULT_INFO, DEFAULT_AUDIT_TRAIL_MESSAGE, FileAction.INCONSISTENCY, DEFAULT_OPERATION_ID, DEFAULT_CERTIFICATE_ID);

        addStep("Extract 3 audit-trails", "Should give first 3 audit-trails in order.");
        AuditTrailDatabaseResults events = daba.getAudits(firstCollectionID, null, null, null, null, null, 3L);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 3L);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().get(0).getActionOnFile(), FileAction.PUT_FILE);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().get(1).getActionOnFile(), FileAction.CHECKSUM_CALCULATED);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().get(2).getActionOnFile(), FileAction.FILE_MOVED);
        
        long firstSeq = events.getAuditTrailEvents().getAuditTrailEvent().get(0).getSequenceNumber().longValue();

        addStep("Extract 3 audit-trails, with larger seq-number than the first", "Should give audit-trail #2, #3, #4");
        events = daba.getAudits(firstCollectionID, null, firstSeq+1, null, null, null, 3L);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 3L);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().get(0).getActionOnFile(), FileAction.CHECKSUM_CALCULATED);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().get(1).getActionOnFile(), FileAction.FILE_MOVED);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().get(2).getActionOnFile(), FileAction.FAILURE);
        
        dm.getConnector().destroy();
    }
    
    @Test(groups = {"regressiontest", "databasetest"})
    public void testAuditTrailDatabaseIngest() throws Exception {
        addDescription("Testing the ingest of data.");
        addStep("Setup varibles and the database connection.", "No errors.");
        String fileID1 = "FILE-ID-1";
        String actor = "ACTOR";
        String info = "Adding a info";
        String auditTrail = "AuditTrail";
        String operationID = "op1";
        String certificateID = "aa";
        String veryLongString = "";
        for(int i = 0; i < 255; i++) {
            veryLongString += i;
        }

        DatabaseManager dm = new AuditDatabaseManager(databaseSpecifics);
        AuditTrailContributerDAO daba = new DerbyAuditTrailContributorDAO(dm);
        daba.initialize(settings.getComponentID());
        
        addStep("Test with all data.", "No failures");
        daba.addAuditEvent(firstCollectionID, fileID1, actor, info, auditTrail, FileAction.FAILURE, operationID, certificateID);
        
        addStep("Test with no collection", "Throws exception");
        try {
            daba.addAuditEvent(null, fileID1, actor, info, auditTrail, FileAction.FAILURE, operationID, certificateID);
            Assert.fail("Should throw an exception");
        } catch (IllegalArgumentException e) {
            // expected
        }
        
        addStep("Test with with no file id.", "No failures");
        daba.addAuditEvent(firstCollectionID, null, actor, info, auditTrail, FileAction.FAILURE, operationID, certificateID);

        addStep("Test with with no actor.", "No failures");
        daba.addAuditEvent(firstCollectionID, fileID1, null, info, auditTrail, FileAction.FAILURE, operationID, certificateID);

        addStep("Test with with no info.", "No failures");
        daba.addAuditEvent(firstCollectionID, fileID1, actor, null, auditTrail, FileAction.FAILURE, operationID, certificateID);

        addStep("Test with with no audittrail.", "No failures");
        daba.addAuditEvent(firstCollectionID, fileID1, actor, info, null, FileAction.FAILURE, operationID, certificateID);

        addStep("Test with with no operationID.", "No failures");
        daba.addAuditEvent(firstCollectionID, fileID1, actor, info, auditTrail, FileAction.FAILURE, null, certificateID);

        addStep("Test with with no certificateID.", "No failures");
        daba.addAuditEvent(firstCollectionID, fileID1, actor, info, auditTrail, FileAction.FAILURE, operationID, null);

        addStep("Test with with no action.", "Throws exception");
        try {
            daba.addAuditEvent(firstCollectionID, fileID1, actor, info, auditTrail, null, operationID, certificateID);
            Assert.fail("Should throw an exception");
        } catch (IllegalArgumentException e) {
            // expected
        }

        addStep("Test with with very large file id.", "Throws exception");
        try {
            daba.addAuditEvent(firstCollectionID, veryLongString, actor, info, auditTrail, FileAction.FAILURE, operationID, certificateID);
            Assert.fail("Should throw an exception");
        } catch (IllegalStateException e) {
            // expected
        }

        addStep("Test with with very large actor name.", "Throws exception");
        try {
            daba.addAuditEvent(firstCollectionID, fileID1, veryLongString, info, auditTrail, FileAction.FAILURE, operationID, certificateID);
            Assert.fail("Should throw an exception");
        } catch (IllegalStateException e) {
            // expected
        }

        addStep("Test with with very large info.", "No failures");
        daba.addAuditEvent(firstCollectionID, fileID1, actor, veryLongString, auditTrail, FileAction.FAILURE, operationID, certificateID);

        addStep("Test with with very large audittrail.", "No failures");
        daba.addAuditEvent(firstCollectionID, fileID1, actor, info, veryLongString, FileAction.FAILURE, operationID, certificateID);
        
        dm.getConnector().destroy();
    }

    /**
     * Helper class which knows how to create a Derby database with an enclosed script.
     */

    private class TestAuditTrailContributorDBCreator extends DatabaseCreator {
        public static final String DEFAULT_AUDIT_TRAIL_DB_SCRIPT = "sql/derby/auditContributorDBCreation.sql";

        public void createAuditTrailContributorDatabase(DatabaseSpecifics databaseSpecifics) {
            createDatabase(databaseSpecifics, DEFAULT_AUDIT_TRAIL_DB_SCRIPT);
        }
    }
}
