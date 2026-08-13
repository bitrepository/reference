/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.IntegrityDatabaseManager;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.DerbyIntegrityDAO;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO;
import org.bitrepository.integrityservice.cache.database.IntegrityDatabaseCreator;
import org.bitrepository.integrityservice.cache.database.PostgresIntegrityDAO;
import org.bitrepository.pillar.integration.PostgresFixedPortContainer;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.service.database.DatabaseUtils;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.xml.datatype.DatatypeConfigurationException;
import java.math.BigInteger;
import java.util.List;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Testcontainers
public abstract class IntegrityDatabaseTestCase {
    public static final String INTEGRITYTEST = "integritytest";
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
    static PostgreSQLContainer integrityDB = new PostgresFixedPortContainer("postgres:18-alpine")
                                                 .withFixedExposedPort(9876, 5432, InternetProtocol.TCP)
                                                 .withDatabaseName("integrityDB")
                                                 .withUsername("testcontainerUser")
                                                 .withPassword("testcontainerPassword")
                                                 .withLabel("purpose", "integrityDB")
                                                 .withClasspathResourceMapping(
                                                     "sql/postgres/integrityDBCreation.sql",
                                                     "/docker-entrypoint-initdb.d/init.sql",
                                                     BindMode.READ_ONLY)
                                                 .withClasspathResourceMapping(
                                                     "sql/postgres/integrityDB7to8migration.sql",
                                                     "/docker-entrypoint-initdb.d/init2.sql",
                                                     BindMode.READ_ONLY);

    @BeforeAll
    public void beforeAll() throws DatatypeConfigurationException {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");
        customizeSettings();
        SettingsUtils.initialize(settings);
    }

    @BeforeEach
    public void beforeEach() {
        clearDatabase();
        new IntegrityDatabaseCreator().createIntegrityDatabase(settings,
                                                               "sql/postgres/integrityDBCreation.sql",
                                                               "sql/postgres/integrityDB7to8migration.sql");
    }

    public void clearDatabase() {
        DBConnector connector = new DBConnector(settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        DatabaseUtils.executeStatement(connector, "DELETE FROM fileinfo");
        DatabaseUtils.executeStatement(connector, "DELETE FROM collection_progress");
        DatabaseUtils.executeStatement(connector, "DELETE FROM pillarstats");
        DatabaseUtils.executeStatement(connector, "DELETE FROM collectionstats");
        DatabaseUtils.executeStatement(connector, "DELETE FROM stats");
        DatabaseUtils.executeStatement(connector, "DELETE FROM pillar");
        DatabaseUtils.executeStatement(connector, "DELETE FROM collections");
    }

    /**
     * Inserts the checksumdata, but ensures that the data can be inserted, by inserting the file-id-data before.
     * @param cache The integrity cache.
     * @param csData The checksum data.
     * @param pillarID The id of the pillar.
     * @param collectionID The id of the collection.
     */
    protected void insertChecksumDataForModel(IntegrityModel cache, List<ChecksumDataForChecksumSpecTYPE> csData,
            String pillarID, String collectionID) {
        insertMissingFilesInChecksumDataForModel(cache, csData, pillarID, collectionID);
        cache.addChecksums(csData, pillarID, collectionID);
    }

    /**
     * Converts a piece of checksum data into file id data.
     * @param csData The checksum data to convert.
     */
    protected void insertMissingFilesInChecksumDataForModel(IntegrityModel cache, List<ChecksumDataForChecksumSpecTYPE> csData,
            String pillarID, String collectionID) {
        FileIDsData res = new FileIDsData();
        FileIDsDataItems items = new FileIDsDataItems();

        for(ChecksumDataForChecksumSpecTYPE entry : csData) {
            FileIDsDataItem dataItem = new FileIDsDataItem();
            dataItem.setFileID(entry.getFileID());
            dataItem.setFileSize(BigInteger.ZERO);
            dataItem.setLastModificationTime(entry.getCalculationTimestamp());
            items.getFileIDsDataItem().add(dataItem);
        }

        res.setFileIDsDataItems(items);
        cache.addFileIDs(res, pillarID, collectionID);
    }

    /**
     * Method to modify the by constructor loaded settings.
     * Default implementation does nothing, so override to change behavior.
     */
    protected void customizeSettings() throws DatatypeConfigurationException { }

    protected IntegrityDAO createDAO() {
        DatabaseSpecifics integrityDatabase = settings.getReferenceSettings()
                                                      .getIntegrityServiceSettings()
                                                      .getIntegrityDatabase();
        DatabaseManager dm = new IntegrityDatabaseManager(integrityDatabase);

        return switch (integrityDatabase.getDriverClass()) {
            case "org.postgresql.Driver" -> new PostgresIntegrityDAO(dm.getConnector());
            default -> new DerbyIntegrityDAO(dm.getConnector());
        };
    }
}
