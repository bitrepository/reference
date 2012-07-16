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

import java.io.File;
import java.sql.Connection;
import java.util.Collection;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.database.DBConnector;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.DatabaseTestUtils;
import org.bitrepository.common.utils.FileUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AuditDatabaseTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    String fileId = "TEST-FILE-ID-" + new Date().getTime();
    String component1 = "ACTOR-1";
    String component2 = "ACTOR-2";
    String DATABASE_NAME = "auditcontributerdb";
    String DATABASE_DIRECTORY = "test-data";
    String DATABASE_URL = "jdbc:derby:" + DATABASE_DIRECTORY + "/" + DATABASE_NAME;
    File dbDir = null;

    @BeforeClass (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings(getClass().getSimpleName());
        settings.getReferenceSettings().getPillarSettings().getAuditTrailContributerDatabase().setDatabaseURL(DATABASE_URL);
        
        addStep("Initialise the database", "Should be unpacked from a jar-file.");
        File dbFile = new File("src/test/resources/auditcontributerdb.jar");
        Assert.assertTrue(dbFile.isFile(), "The database file should exist");
        
        dbDir = FileUtils.retrieveDirectory(DATABASE_DIRECTORY);
        FileUtils.retrieveSubDirectory(dbDir, DATABASE_NAME);
        
        Connection dbCon = DatabaseTestUtils.takeDatabase(dbFile, DATABASE_NAME, dbDir);
        dbCon.close();
    }

    @AfterClass (alwaysRun = true)
    public void shutdown() throws Exception {
        addStep("Cleanup after test.", "Should remove directory with test material.");
        if(dbDir != null) {
            FileUtils.delete(dbDir);
        }
    }

    @Test(groups = {"regressiontest", "databasetest"})
    public void testFileBasedCacheFunctions() throws Exception {
        addDescription("Testing the basic functions of the audit trail database interface.");
        addStep("Setup varibles and the database connection.", "No errors.");
        String fileId1 = "FILE-ID-1";
        String fileId2 = "FILE-ID-2";
        String actor = "ACTOR";
        String info = "Adding a info";
        String auditTrail = "AuditTrail";
        DBConnector dbConnector = new DBConnector(settings.getReferenceSettings().getPillarSettings().getAuditTrailContributerDatabase());
        AuditTrailContributerDAO daba = new AuditTrailContributerDAO(settings, dbConnector);
        
        addStep("Populate the database.", "Should be inserted into database.");
        daba.addAuditEvent(fileId1, actor, info, auditTrail, FileAction.PUT_FILE);
        daba.addAuditEvent(fileId1, actor, info, auditTrail, FileAction.CHECKSUM_CALCULATED);
        daba.addAuditEvent(fileId2, actor, info, auditTrail, FileAction.FILE_MOVED);
        daba.addAuditEvent(fileId2, actor, info, auditTrail, FileAction.FAILURE);
        daba.addAuditEvent(fileId2, actor, info, auditTrail, FileAction.INCONSISTENCY);
        
        addStep("Test output", "Should be valid.");
        Collection<AuditTrailEvent> events = daba.getAudits(null, null, null, null, null);
        Assert.assertEquals(events.size(), 5);
        
        events = daba.getAudits(fileId1, null, null, null, null);
        Assert.assertEquals(events.size(), 2);        

        events = daba.getAudits(fileId2, null, null, null, null);
        Assert.assertEquals(events.size(), 3);
        
        Long seq = daba.extractLargestSequenceNumber();
        
        events = daba.getAudits(null, seq, null, null, null);
        Assert.assertEquals(events.size(), 1);
        
        events = daba.getAudits(fileId1, seq-3, null, null, null);
        Assert.assertEquals(events.size(), 1);
        
        dbConnector.cleanup();
    }
}
