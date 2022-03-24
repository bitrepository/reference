/*
 * #%L
 * Bitrepository Service
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.audittrails.store;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;

import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDIT_TRAIL_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.DATABASE_VERSION_ENTRY;
import static org.testng.Assert.assertEquals;

// TODO: cannot test migration of version 1 to 2, since it requires a collection id.
// Therefore this is only tested with version 2 of the database.
public class AuditServiceDatabaseMigrationTest extends ExtendedTestCase {
    protected Settings settings;
    
    static final String PATH_TO_DATABASE_UNPACKED = "target/test/audits/auditservicedb-v2";
    static final String PATH_TO_DATABASE_JAR_FILE = "src/test/resources/auditservicedb-v2.jar";
    
    static final String FILE_ID = "default-file-id";

    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("ReferencePillarTest");
        
        settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailServiceDatabase().setDatabaseURL(
                "jdbc:derby:" + PATH_TO_DATABASE_UNPACKED + "/auditservicedb");

        DatabaseSpecifics auditDB =
                settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailServiceDatabase();
        DerbyDatabaseDestroyer.deleteDatabase(auditDB);

        FileUtils.unzip(new File(PATH_TO_DATABASE_JAR_FILE), FileUtils.retrieveDirectory(PATH_TO_DATABASE_UNPACKED));
    }
    
    @AfterMethod (alwaysRun = true)
    public void cleanup() throws Exception {
        FileUtils.deleteDirIfExists(new File(PATH_TO_DATABASE_UNPACKED));
    }
    
    @Test( groups = {"regressiontest", "databasetest"})
    public void testMigratingAuditServiceDatabase() {
        addDescription("Tests that the database can be migrated to latest version with the provided scripts.");
        DBConnector connector = new DBConnector(
                settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditTrailServiceDatabase());

        addStep("Validate setup", "audit table has version 2 and database version 2");
        String extractVersionSql = "SELECT version FROM tableversions WHERE tablename = ?";
        int auditTableVersionBefore = DatabaseUtils.selectIntValue(connector, extractVersionSql, AUDIT_TRAIL_TABLE);
        assertEquals(auditTableVersionBefore, 2, "Table version before migration");
        int dbTableVersionBefore = DatabaseUtils.selectIntValue(connector, extractVersionSql, DATABASE_VERSION_ENTRY);
        assertEquals(dbTableVersionBefore, 2, "Table version before migration");
        
        addStep("Perform migration", "audit table version 5 and database-version is 6");
        AuditTrailServiceDatabaseMigrator migrator = new AuditTrailServiceDatabaseMigrator(connector);
        migrator.migrate();
        int auditTableVersionAfter = DatabaseUtils.selectIntValue(connector, extractVersionSql, AUDIT_TRAIL_TABLE);
        assertEquals(auditTableVersionAfter, 5, "Table version after migration");
        int dbTableVersionAfter = DatabaseUtils.selectIntValue(connector, extractVersionSql, DATABASE_VERSION_ENTRY);
        assertEquals(dbTableVersionAfter, 6, "Table version after migration");
    }
}
