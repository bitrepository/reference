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

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumDBMigrator;
import org.bitrepository.pillar.store.checksumdatabase.DatabaseConstants;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.Instant;
import java.time.ZoneId;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;
import static org.bitrepository.pillar.store.checksumdatabase.DatabaseConstants.CHECKSUM_TABLE;

class ChecksumDatabaseMigrationTest {
    protected Settings settings;

    static final String PATH_TO_DATABASE_UNPACKED = "target/test/referencepillar/checksumdb-for-migration";
    static final String PATH_TO_DATABASE_V1_JAR_FILE = "src/test/resources/checksumdb-version1.jar";
    static final String PATH_TO_DATABASE_V3_JAR_FILE = "src/test/resources/checksumdb-version3.jar";

    static final String FILE_ID = "default-file-id";
    static final String CHECKSUM = "default-checksum";

    static DBConnector connector = null;

    @BeforeEach
    void setup() {
        settings = TestSettingsProvider.reloadSettings("ReferencePillarTest");

        settings.getReferenceSettings().getPillarSettings().getChecksumDatabase().setDatabaseURL(
                "jdbc:derby:" + PATH_TO_DATABASE_UNPACKED + "/checksumdb");

        DatabaseSpecifics checksumDB =
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase();
        DerbyDatabaseDestroyer.deleteDatabase(checksumDB);
    }

    @AfterEach
    void cleanup() throws Exception {
        DatabaseSpecifics checksumDB = settings.getReferenceSettings().getPillarSettings().getChecksumDatabase();
        if (connector != null) {
            if (connector.getConnection() != null && !connector.getConnection().isClosed()) {
                connector.getConnection().close();
            }
            connector.destroy();
            connector = null;
        }
        DerbyDatabaseDestroyer.deleteDatabase(checksumDB);
        FileUtils.deleteDirIfExists(new File(PATH_TO_DATABASE_UNPACKED));
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void testMigratingChecksumDatabaseFromV1ToV2() throws Exception {
        addDescription("Tests that the checksums table can be migrated from version 1 to 2, " +
                "e.g. getting the column collectionid, which should be set to the default in settings.");
        addStep("Unzipping and connecting to checksum database version 1", "");
        FileUtils.unzip(new File(PATH_TO_DATABASE_V1_JAR_FILE), FileUtils.retrieveDirectory(PATH_TO_DATABASE_UNPACKED));

        connector = new DBConnector(
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase());

        addStep("Validate setup", "Checksums table has version 1");
        String extractVersionSql = "SELECT version FROM tableversions WHERE tablename = ?";
        int versionBefore = DatabaseUtils.selectIntValue(connector, extractVersionSql, CHECKSUM_TABLE);
        Assertions.assertEquals(1, versionBefore, "Table version before migration");

        addStep("Ingest a entry to the database without the collection id",
                "works only in version 1.");
        String insertSql =
                "INSERT INTO " + DatabaseConstants.CHECKSUM_TABLE + " ( " + DatabaseConstants.CS_FILE_ID + " , " +
                        DatabaseConstants.CS_CHECKSUM + " , " + DatabaseConstants.CS_DATE
                        + " ) VALUES ( ? , ? , ? )";
        DatabaseUtils.executeStatement(connector, insertSql, FILE_ID, CHECKSUM, Instant.now());

        addStep("Perform migration", "Checksums table has version 3");
        ChecksumDBMigrator migrator = new ChecksumDBMigrator(connector, settings);
        migrator.migrate();
        int versionAfter = DatabaseUtils.selectIntValue(connector, extractVersionSql, CHECKSUM_TABLE);
        Assertions.assertEquals(4, versionAfter, "Table version after migration");

        addStep("Validate the entry",
                "The collection id has been set to the default collection id");
        String retrieveCollectionIdSql =
                "SELECT " + DatabaseConstants.CS_COLLECTION_ID + " FROM " + DatabaseConstants.CHECKSUM_TABLE + " WHERE "
                        + DatabaseConstants.CS_FILE_ID + " = ?";
        String collectionID = DatabaseUtils.selectStringValue(connector, retrieveCollectionIdSql, FILE_ID);
        Assertions.assertEquals(settings.getCollections().get(0).getID(), collectionID);
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    void testMigratingChecksumDatabaseFromV3ToV4() throws Exception {
        addDescription("Tests that the checksums table can be migrated from version 3 to 4, " +
                "e.g. changing the column calculatedchecksumdate from timestamp to bigint.");
        addStep("Ensure cleanup", "");
        addStep("Unzipping and connecting to checksum database version 3", "");
        FileUtils.unzip(new File(PATH_TO_DATABASE_V3_JAR_FILE), FileUtils.retrieveDirectory(PATH_TO_DATABASE_UNPACKED));

        connector = new DBConnector(
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase());
        Instant testInstant = Instant.parse("2016-01-28T12:31:43.527Z");
        Assertions.assertFalse(connector.getConnection().isClosed());

        addStep("Validate setup", "Checksums table has version 3");
        String extractVersionSql = "SELECT version FROM tableversions WHERE tablename = ?";
        int versionBefore = DatabaseUtils.selectIntValue(connector, extractVersionSql, CHECKSUM_TABLE);
        Assertions.assertEquals(3, versionBefore, "Table version before migration");

        addStep("Ingest a entry to the database with a date for the calculationdate",
                "works in version 3.");
        String insertSql =
                "INSERT INTO " + DatabaseConstants.CHECKSUM_TABLE + " ( " + DatabaseConstants.CS_FILE_ID + " , " +
                        DatabaseConstants.CS_CHECKSUM + " , " + DatabaseConstants.CS_DATE
                        + " , " + DatabaseConstants.CS_COLLECTION_ID + " ) VALUES ( ? , ? , ? , ? )";
        DatabaseUtils.executeStatement(connector, insertSql, FILE_ID, CHECKSUM, testInstant,
                settings.getCollections().get(0).getID());

        addStep("Perform migration", "Checksums table has version 4");
        ChecksumDBMigrator migrator = new ChecksumDBMigrator(connector, settings);
        migrator.migrate();
        int versionAfter = DatabaseUtils.selectIntValue(connector, extractVersionSql, CHECKSUM_TABLE);
        Assertions.assertEquals(4, versionAfter, "Table version after migration");

        addStep("Validate the migration", "The timestamp is now the millis from epoch");
        String retrieveCollectionIdSql =
                "SELECT " + DatabaseConstants.CS_DATE + " FROM " + DatabaseConstants.CHECKSUM_TABLE + " WHERE "
                        + DatabaseConstants.CS_FILE_ID + " = ?";
        Long extractedDate = DatabaseUtils.selectFirstLongValue(connector, retrieveCollectionIdSql, FILE_ID);

        long testDateAtTimeZone = testInstant.toEpochMilli()
                + ZoneId.systemDefault().getRules().getStandardOffset(testInstant).getTotalSeconds() * 1000L;

        Assertions.assertNotNull(extractedDate);
        Assertions.assertEquals(testDateAtTimeZone, extractedDate.longValue());
    }
}
