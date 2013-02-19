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

import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseCreator;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AuditTrailContributorDatabaseTest extends ExtendedTestCase {
    private Settings settings;
    private DatabaseSpecifics databaseSpecifics;

    @BeforeClass (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings(getClass().getSimpleName());

        databaseSpecifics = new DatabaseSpecifics();
        databaseSpecifics.setDriverClass("org.apache.derby.jdbc.EmbeddedDriver");
        databaseSpecifics.setDatabaseURL("jdbc:derby:target/test/auditcontributerdb");

        DerbyDatabaseDestroyer.deleteDatabase(databaseSpecifics);

        TestAuditTrailContributorDBCreator dbCreator = new TestAuditTrailContributorDBCreator();
        dbCreator.createAuditTrailContributorDatabase(databaseSpecifics);
    }

    @Test(groups = {"regressiontest", "databasetest"})
    public void testAuditTrailDatabaseFunctions() throws Exception {
        addDescription("Testing the basic functions of the audit trail database interface.");
        addStep("Setup varibles and the database connection.", "No errors.");
        String fileId1 = "FILE-ID-1";
        String fileId2 = "FILE-ID-2";
        String actor = "ACTOR";
        String info = "Adding a info";
        String auditTrail = "AuditTrail";
        DBConnector dbConnector = new DBConnector(databaseSpecifics);
        AuditTrailContributerDAO daba = new AuditTrailContributerDAO(settings, dbConnector);
        
        addStep("Populate the database.", "Should be inserted into database.");
        daba.addAuditEvent(settings.getCollectionID(), fileId1, actor, info, auditTrail, FileAction.PUT_FILE);
        daba.addAuditEvent(settings.getCollectionID(), fileId1, actor, info, auditTrail, FileAction.CHECKSUM_CALCULATED);
        daba.addAuditEvent(settings.getCollectionID(), fileId2, actor, info, auditTrail, FileAction.FILE_MOVED);
        daba.addAuditEvent(settings.getCollectionID(), fileId2, actor, info, auditTrail, FileAction.FAILURE);
        daba.addAuditEvent(settings.getCollectionID(), fileId2, actor, info, auditTrail, FileAction.INCONSISTENCY);
        
        addStep("Test extracting all the events", "Should be all 5 events.");
        AuditTrailDatabaseResults events = daba.getAudits(settings.getCollectionID(), null, null, null, null, null, null);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 5);
        
        addStep("Test extracting the events for fileID1", "Should be 2 events.");
        events = daba.getAudits(settings.getCollectionID(), fileId1, null, null, null, null, null);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 2);

        addStep("Test extracting the events for fileID2", "Should be 3 events.");
        events = daba.getAudits(settings.getCollectionID(), fileId2, null, null, null, null, null);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 3);
        
        addStep("Test extracting the events with the sequence number at least equal to the largest sequence number.", 
                "Should be 1 event.");
        Long seq = daba.extractLargestSequenceNumber();
        events = daba.getAudits(settings.getCollectionID(), null, seq, null, null, null, null);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 1);
        
        addStep("Test extracting the events for fileID1 with sequence number 2 or more", "Should be 1 event.");
        events = daba.getAudits(settings.getCollectionID(), fileId1, seq-3, null, null, null, null);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 1);

        addStep("Test extracting the events for fileID1 with at most sequence number 2", "Should be 2 events.");
        events = daba.getAudits(settings.getCollectionID(), fileId1, null, seq-3, null, null, null);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 2);

        addStep("Test extracting at most 3 events", "Should extract 3 events.");
        events = daba.getAudits(settings.getCollectionID(), null, null, null, null, null, 3L);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 3);

        addStep("Test extracting at most 1000 events", "Should extract all 5 events.");
        events = daba.getAudits(settings.getCollectionID(), null, null, null, null, null, 1000L);
        Assert.assertEquals(events.getAuditTrailEvents().getAuditTrailEvent().size(), 5);
        
        dbConnector.destroy();
    }

    private class TestAuditTrailContributorDBCreator extends DatabaseCreator {
        public static final String DEFAULT_AUDIT_TRAIL_DB_SCRIPT = "sql/derby/auditContributorDBCreation.sql";

        public void createAuditTrailContributorDatabase(DatabaseSpecifics databaseSpecifics) {
            createDatabase(databaseSpecifics, DEFAULT_AUDIT_TRAIL_DB_SCRIPT);
        }
    }
}
