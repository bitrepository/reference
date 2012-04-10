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
package org.bitrepository.pillar.audit;

import java.sql.Connection;
import java.util.Collection;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.database.DatabaseUtils;
import org.bitrepository.common.database.DerbyDBConnector;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AuditDatabaseTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    
    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings();
    }
    
    @AfterMethod (alwaysRun=true) 
    public void closeArchive() throws Exception {
        clearDatabase(settings.getReferenceSettings().getPillarSettings().getAuditContributerDatabaseUrl());
    }


    @Test( groups = {"databasetest"})
    public void testFileBasedCacheFunctions() throws Exception {
        addDescription("Testing the basic functions of the audit trail database interface.");
        addStep("Setup varibles and the database connection.", "No errors.");
        String fileId1 = "FILE-ID-1";
        String fileId2 = "FILE-ID-2";
        String actor = "ACTOR";
        String info = "Adding a info";
        String auditTrail = "AuditTrail";
        AuditTrailContributerDAO daba = new AuditTrailContributerDAO(settings);
        
        addStep("Populate the database.", "Should be inserted into database.");
        daba.addAuditEvent(fileId1, actor, info, auditTrail, FileAction.PUT_FILE);
        daba.addAuditEvent(fileId1, actor, info, auditTrail, FileAction.CHECKSUM_CALCULATED);
        daba.addAuditEvent(fileId2, actor, info, auditTrail, FileAction.FILE_MOVED);
        daba.addAuditEvent(fileId2, actor, info, auditTrail, FileAction.FAILURE);
        daba.addAuditEvent(fileId2, actor, info, auditTrail, FileAction.INCONSISTENCY);
        
        addStep("Test output", "Should be valid.");
        Collection<AuditTrailEvent> events = daba.getAudits(null, null);
        Assert.assertEquals(events.size(), 5);
        
        events = daba.getAudits(fileId1, null);
        Assert.assertEquals(events.size(), 2);        

        events = daba.getAudits(fileId2, null);
        Assert.assertEquals(events.size(), 3);
        
        Long seq = daba.extractLargestSequenceNumber();
        
        events = daba.getAudits(null, seq);
        Assert.assertEquals(events.size(), 1);
        
        events = daba.getAudits(fileId1, seq-3);
        Assert.assertEquals(events.size(), 1);
    }
    
    /**
     * Cleans up the database after use.
     * @param url The URL to the database.
     * @throws Exception If anything goes bad.
     */
    private void clearDatabase(String url) throws Exception {
        Connection con = new DerbyDBConnector().getEmbeddedDBConnection(url);
        
        String sqlActor = "DELETE FROM " + AuditDatabaseConstants.ACTOR_TABLE;
        DatabaseUtils.executeStatement(con, sqlActor, new Object[0]);
        String sqlAudit = "DELETE FROM " + AuditDatabaseConstants.AUDITTRAIL_TABLE;
        DatabaseUtils.executeStatement(con, sqlAudit, new Object[0]);
        String sqlFile = "DELETE FROM " + AuditDatabaseConstants.FILE_TABLE;
        DatabaseUtils.executeStatement(con, sqlFile, new Object[0]);
    }
}
