/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.stresstest;

import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.integrityservice.cache.IntegrityDatabaseManager;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO;
import org.bitrepository.integrityservice.cache.database.IntegrityDatabaseCreator;
import org.bitrepository.integrityservice.cache.database.PostgresIntegrityDAO;
import org.bitrepository.pillar.integration.PostgresFixedPortContainer;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.service.database.DatabaseUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;
import javax.xml.datatype.XMLGregorianCalendar;
import java.math.BigInteger;
import java.time.Instant;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
class DatabaseStressTests {

    private static final String PILLAR_1 = "pillar1";
    private static final String PILLAR_2 = "pillar2";
    private static final String PILLAR_3 = "pillar3";
    private static final String PILLAR_4 = "pillar4";

    private static final Integer NUMBER_OF_FILES = 10000;

    protected Settings settings;

    /// Is NOT unused
    ///
    /// Creates a postgres server on port 9876 which match the requirement from
    /// ```
    /// <integritydatabase>
    ///     <driverclass>org.postgresql.Driver</driverclass>
    ///     <databaseurl>jdbc:postgresql://localhost:9876/integrityDB</databaseurl>
    ///     <username>testcontainerUser</username>
    ///     <password>testcontainerPassword</password>
    /// </integritydatabase>
    /// ```
    /// from bitrepository-core/src/test/resources/settings/xml/bitrepository-devel/ReferenceSettings.xml
    @Container
    static PostgreSQLContainer postgreSQLContainer = new PostgresFixedPortContainer("postgres:18-alpine")
                                                             .withFixedExposedPort(9876, 5432, InternetProtocol.TCP)
                                                             .withDatabaseName("integrityDB")
                                                             .withUsername("testcontainerUser")
                                                             .withPassword("testcontainerPassword")
                                                             .withLabel("purpose","integrityDB");

    @BeforeAll
    public void beforeAll() throws Exception {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");
        SettingsUtils.initialize(settings);
        IntegrityDatabaseCreator integrityDatabaseCreator = new IntegrityDatabaseCreator();
        integrityDatabaseCreator.createIntegrityDatabase(settings, "sql/postgres/integrityDBCreation.sql", "sql/postgres/integrityDB7to8migration.sql");

    }

    @BeforeEach
    public void beforeEach() throws Exception {
        clearDatabase();
        IntegrityDatabaseCreator integrityDatabaseCreator = new IntegrityDatabaseCreator();
        integrityDatabaseCreator.createIntegrityDatabase(settings, "sql/postgres/integrityDBCreation.sql", "sql/postgres/integrityDB7to8migration.sql");

        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_1);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_2);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_3);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_4);

        Duration time = DatatypeFactory.newInstance().newDuration(0);
        settings.getReferenceSettings().getIntegrityServiceSettings().setTimeBeforeMissingFileCheck(time);
        SettingsUtils.initialize(settings);

    }
    public void clearDatabase() {
        DBConnector connector = new DBConnector(settings.getReferenceSettings()
                                                        .getIntegrityServiceSettings()
                                                        .getIntegrityDatabase());
        DatabaseUtils.executeStatement(connector, "DELETE FROM fileinfo");
        DatabaseUtils.executeStatement(connector, "DELETE FROM collection_progress");
        DatabaseUtils.executeStatement(connector, "DELETE FROM pillarstats");
        DatabaseUtils.executeStatement(connector, "DELETE FROM collectionstats");
        DatabaseUtils.executeStatement(connector, "DELETE FROM stats");
        DatabaseUtils.executeStatement(connector, "DELETE FROM pillar");
        DatabaseUtils.executeStatement(connector, "DELETE FROM collections");
    }

    protected void populateDatabase(IntegrityDAO cache) {
        FileIDsData data = new FileIDsData();
        FileIDsDataItems items = new FileIDsDataItems();
        XMLGregorianCalendar lastModificationTime = CalendarUtils.getNow();
        for (int i = 0; i < NUMBER_OF_FILES; i++) {
            FileIDsDataItem item = new FileIDsDataItem();
            item.setFileID("fileid-" + i);
            item.setFileSize(BigInteger.valueOf(i));
            item.setLastModificationTime(lastModificationTime);
            items.getFileIDsDataItem().add(item);
        }
        data.setFileIDsDataItems(items);
        String collectionID = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
        cache.updateFileIDs(data, PILLAR_1, collectionID);
        cache.updateFileIDs(data, PILLAR_2, collectionID);
        cache.updateFileIDs(data, PILLAR_3, collectionID);
        cache.updateFileIDs(data, PILLAR_4, collectionID);
    }

    @Test
    @Tag(TestGroups.STRESS_TEST)
    @Tag("integritytest")
    void testDatabasePerformance() {
        addDescription("Testing the performance of the SQL queries to the database.");
        IntegrityDAO cache = createDAO();
        Assertions.assertNotNull(cache);

        long startTime = System.currentTimeMillis();
        populateDatabase(cache);
        System.err.println("Time to ingest '" + NUMBER_OF_FILES + "' files: " + TimeUtils.millisecondsToHuman(System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        String collection = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
        int numberOfPillarsInCollection =
                settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().size();
        cache.findFilesWithMissingCopies(collection, numberOfPillarsInCollection, 0L, Long.MAX_VALUE);
        System.err.println("Time to find missing files: " + TimeUtils.millisecondsToHuman(System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        for (String pillar : settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID()) {
            cache.getFilesWithMissingChecksums(collection, pillar, Instant.EPOCH);
        }
        System.err.println("Time to find missing checksums: " + TimeUtils.millisecondsToHuman(System.currentTimeMillis() - startTime));
    }

    private IntegrityDAO createDAO() {
        DatabaseManager dm = new IntegrityDatabaseManager(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        return new PostgresIntegrityDAO(dm.getConnector());
    }

}
