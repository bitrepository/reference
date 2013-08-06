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
package org.bitrepository.service.audit;

import java.io.File;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.bitrepository.service.audit.AuditDatabaseConstants.FILE_COLLECTIONID;
import static org.bitrepository.service.audit.AuditDatabaseConstants.FILE_FILEID;
import static org.bitrepository.service.audit.AuditDatabaseConstants.FILE_TABLE;

public class AuditTrailContributorDatabaseMigrationTest extends ExtendedTestCase {
    protected Settings settings;
    
    static final String PATH_TO_DATABASE_UNPACKED = "target/test/audits/auditcontributerdb-v1";
    static final String PATH_TO_DATABASE_JAR_FILE = "src/test/resources/auditcontributerdb-v1.jar";
    
    static final String FILE_ID = "default-file-id";

    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("ReferencePillarTest");
        
        settings.getReferenceSettings().getPillarSettings().getAuditTrailContributerDatabase().setDatabaseURL(
                "jdbc:derby:" + PATH_TO_DATABASE_UNPACKED + "/auditcontributerdb");

        DatabaseSpecifics auditDB =
                settings.getReferenceSettings().getPillarSettings().getAuditTrailContributerDatabase();
        DerbyDatabaseDestroyer.deleteDatabase(auditDB);

        FileUtils.unzip(new File(PATH_TO_DATABASE_JAR_FILE), FileUtils.retrieveDirectory(PATH_TO_DATABASE_UNPACKED));
    }
    
    @AfterMethod (alwaysRun = true)
    public void cleanup() throws Exception {
        FileUtils.deleteDirIfExists(new File(PATH_TO_DATABASE_UNPACKED));
    }
    
//    @Test( groups = {"regressiontest", "databasetest"})
    public void testMigratingDatabaseAuditTrailsContributorFileTable() {
        addDescription("Tests that the file table can be migrated from version 1 to 2, e.g. getting the column "
                + "collectionid, which should be set to the default in settings.");
        DBConnector connector = new DBConnector(
                settings.getReferenceSettings().getPillarSettings().getAuditTrailContributerDatabase());

        addStep("Validate setup", "Checksums table has version 1");
        String extractVersionSql = "SELECT version FROM tableversions WHERE tablename = ?";
        int versionBefore = DatabaseUtils.selectIntValue(connector, extractVersionSql, FILE_TABLE);
        Assert.assertEquals(versionBefore, 1, "Table version before migration");
        
        addStep("Ingest a entry to the database without the collection id", "works only in version 1.");
        String sqlInsert = "INSERT INTO " + FILE_TABLE + " ( " + FILE_FILEID + " ) VALUES ( ? )";
        DatabaseUtils.executeStatement(connector, sqlInsert, FILE_ID);
        
        addStep("Perform migration", "File table has version 2");
        AuditTrailContributorDatabaseMigrator migrator = new AuditTrailContributorDatabaseMigrator(connector, settings);
        migrator.migrate();
        int versionAfter = DatabaseUtils.selectIntValue(connector, extractVersionSql, FILE_TABLE);
        Assert.assertEquals(versionAfter, 2, "Table version after migration");
        
        addStep("Validate the entry", "The collection id has been set to the default collection id");
        String retrieveCollectionIdSql = "SELECT " + FILE_COLLECTIONID + " FROM " + FILE_TABLE + " WHERE " 
                + FILE_FILEID + " = ?";
        String collectionId = DatabaseUtils.selectStringValue(connector, retrieveCollectionIdSql, FILE_ID);
        Assert.assertEquals(collectionId, settings.getCollections().get(0).getID());
    }
}
