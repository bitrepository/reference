/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.store.checksumcache;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumDBMigrator;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.bitrepository.pillar.store.checksumdatabase.DatabaseConstants.CHECKSUM_TABLE;
import static org.bitrepository.pillar.store.checksumdatabase.DatabaseConstants.CS_CHECKSUM;
import static org.bitrepository.pillar.store.checksumdatabase.DatabaseConstants.CS_COLLECTION_ID;
import static org.bitrepository.pillar.store.checksumdatabase.DatabaseConstants.CS_DATE;
import static org.bitrepository.pillar.store.checksumdatabase.DatabaseConstants.CS_FILE_ID;

public class ChecksumDatabaseMigrationTest extends ExtendedTestCase {
    protected Settings settings;
    
    static final String PATH_TO_DATABASE_UNPACKED = "target/test/referencepillar/checksumdb-for-migration";
    static final String PATH_TO_DATABASE_V1_JAR_FILE = "src/test/resources/checksumdb-version1.jar";
    static final String PATH_TO_DATABASE_V3_JAR_FILE = "src/test/resources/checksumdb-version3.jar";
    
    static final String FILE_ID = "default-file-id";
    static final String CHECKSUM = "default-checksum";
    
    static DBConnector connector = null;

    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("ReferencePillarTest");
        
        settings.getReferenceSettings().getPillarSettings().getChecksumDatabase().setDatabaseURL(
                "jdbc:derby:" + PATH_TO_DATABASE_UNPACKED + "/checksumdb");

        DatabaseSpecifics checksumDB =
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase();
        DerbyDatabaseDestroyer.deleteDatabase(checksumDB);
    }
    
    @AfterMethod (alwaysRun = true)
    public void cleanup() throws Exception {
        FileUtils.deleteDirIfExists(new File(PATH_TO_DATABASE_UNPACKED));
        if(connector != null && !connector.getConnection().isClosed()) {
            connector.getConnection().close();
            connector.destroy();
            connector = null;
        }
    }
    
//    @Test( groups = {"regressiontest", "pillartest"})
    public void testMigratingChecksumDatabaseFromV1ToV2() throws Exception {
        addDescription("Tests that the checksums table can be migrated from version 1 to 2, e.g. getting the column "
                + "collectionid, which should be set to the default in settings.");
        addStep("Unzipping and connecting to checksum database version 1", "");
        FileUtils.unzip(new File(PATH_TO_DATABASE_V1_JAR_FILE), FileUtils.retrieveDirectory(PATH_TO_DATABASE_UNPACKED));
        
        connector = new DBConnector(
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase());

        addStep("Validate setup", "Checksums table has version 1");
        String extractVersionSql = "SELECT version FROM tableversions WHERE tablename = ?";
        int versionBefore = DatabaseUtils.selectIntValue(connector, extractVersionSql, CHECKSUM_TABLE);
        Assert.assertEquals(versionBefore, 1, "Table version before migration");
        
        addStep("Ingest a entry to the database without the collection id", "works only in version 1.");
        String insertSql = "INSERT INTO " + CHECKSUM_TABLE + " ( " + CS_FILE_ID + " , " + CS_CHECKSUM + " , " + CS_DATE 
                + " ) VALUES ( ? , ? , ? )";
        DatabaseUtils.executeStatement(connector, insertSql, FILE_ID, CHECKSUM, new Date());
        
        addStep("Perform migration", "Checksums table has version 3");
        ChecksumDBMigrator migrator = new ChecksumDBMigrator(connector, settings);
        migrator.migrate();
        int versionAfter = DatabaseUtils.selectIntValue(connector, extractVersionSql, CHECKSUM_TABLE);
        Assert.assertEquals(versionAfter, 4, "Table version after migration");
        
        addStep("Validate the entry", "The collection id has been set to the default collection id");
        String retrieveCollectionIdSql = "SELECT " + CS_COLLECTION_ID + " FROM " + CHECKSUM_TABLE + " WHERE " 
                + CS_FILE_ID + " = ?";
        String collectionID = DatabaseUtils.selectStringValue(connector, retrieveCollectionIdSql, FILE_ID);
        Assert.assertEquals(collectionID, settings.getCollections().get(0).getID());
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testMigratingChecksumDatabaseFromV3ToV4() throws Exception {
        addDescription("Tests that the checksums table can be migrated from version 3 to 4, e.g. changing the column "
                + "calculatedchecksumdate from timestamp to bigint.");
        addStep("Ensure cleanup", "");
        cleanup();
        
        addStep("Unzipping and connecting to checksum database version 3", "");
        FileUtils.unzip(new File(PATH_TO_DATABASE_V3_JAR_FILE), FileUtils.retrieveDirectory(PATH_TO_DATABASE_UNPACKED));
        
        connector = new DBConnector(
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase());
        Date testDate = new Date(1453984303527L);
        Assert.assertFalse(connector.getConnection().isClosed());
        
        addStep("Validate setup", "Checksums table has version 3");
        String extractVersionSql = "SELECT version FROM tableversions WHERE tablename = ?";
        int versionBefore = DatabaseUtils.selectIntValue(connector, extractVersionSql, CHECKSUM_TABLE);
        Assert.assertEquals(versionBefore, 3, "Table version before migration");
        
        addStep("Ingest a entry to the database with a date for the calculationdate", "works in version 3.");
        String insertSql = "INSERT INTO " + CHECKSUM_TABLE + " ( " + CS_FILE_ID + " , " + CS_CHECKSUM + " , " + CS_DATE
                + " , " + CS_COLLECTION_ID + " ) VALUES ( ? , ? , ? , ? )";
        DatabaseUtils.executeStatement(connector, insertSql, FILE_ID, CHECKSUM, testDate, settings.getCollections().get(0).getID());
        
        addStep("Perform migration", "Checksums table has version 4");
        ChecksumDBMigrator migrator = new ChecksumDBMigrator(connector, settings);
        migrator.migrate();
        int versionAfter = DatabaseUtils.selectIntValue(connector, extractVersionSql, CHECKSUM_TABLE);
        Assert.assertEquals(versionAfter, 4, "Table version after migration");
        
        addStep("Validate the migration", "The timestamp is now the millis from epoch");
        String retrieveCollectionIdSql = "SELECT " + CS_DATE + " FROM " + CHECKSUM_TABLE + " WHERE " 
                + CS_FILE_ID + " = ?";
        Long extractedDate = DatabaseUtils.selectFirstLongValue(connector, retrieveCollectionIdSql, FILE_ID);
        
        Date testDateAtTimeZone = new Date(testDate.getTime()
                + Calendar.getInstance(TimeZone.getDefault(), Locale.ROOT).getTimeZone().getRawOffset());
        
        Assert.assertEquals(extractedDate.longValue(), testDateAtTimeZone.getTime());
    }
}
