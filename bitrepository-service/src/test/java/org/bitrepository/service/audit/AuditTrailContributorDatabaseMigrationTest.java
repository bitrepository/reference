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

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.junit.jupiter.api.*;

import java.io.File;

import static org.bitrepository.protocol.utils.AllureTestUtils.addDescription;
import static org.bitrepository.protocol.utils.AllureTestUtils.addStep;

/**
 * Test database migration.  Generates jaccept reports.
 *
 */
public class AuditTrailContributorDatabaseMigrationTest {
    protected Settings settings;

    static final String PATH_TO_DATABASE_UNPACKED = "target/test/audits/auditcontributerdb-v1";
    static final String PATH_TO_DATABASE_JAR_FILE = "src/test/resources/auditcontributerdb-v1.jar";

    static final String FILE_ID = "default-file-id";

    @BeforeEach
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("ReferencePillarTest");

        settings.getReferenceSettings().getPillarSettings().getAuditTrailContributerDatabase().setDatabaseURL(
                "jdbc:derby:" + PATH_TO_DATABASE_UNPACKED + "/auditcontributerdb");

        DatabaseSpecifics auditDB =
                settings.getReferenceSettings().getPillarSettings().getAuditTrailContributerDatabase();
        DerbyDatabaseDestroyer.deleteDatabase(auditDB);

        FileUtils.unzip(new File(PATH_TO_DATABASE_JAR_FILE), FileUtils.retrieveDirectory(PATH_TO_DATABASE_UNPACKED));
    }

    @AfterEach
    public void cleanup() throws Exception {
        FileUtils.deleteDirIfExists(new File(PATH_TO_DATABASE_UNPACKED));
    }

    @Test
    @Tag("regressiontest")
    @Tag("databasetest")
    public void testMigratingAuditContributorDatabase() {
        addDescription("Tests that the database can be migrated to latest version with the provided scripts.");
        DBConnector connector = new DBConnector(
                settings.getReferenceSettings().getPillarSettings().getAuditTrailContributerDatabase());

        addStep("Validate setup", "File table and audit table has version 1 ");
        String extractVersionSql = "SELECT version FROM tableversions WHERE tablename = ?";
        int fileTableVersionBefore = DatabaseUtils.selectIntValue(connector, extractVersionSql,
                AuditDatabaseConstants.FILE_TABLE);
        Assertions.assertEquals(1, fileTableVersionBefore, "File table before migration");
        int auditTableVersionBefore = DatabaseUtils.selectIntValue(connector, extractVersionSql,
                AuditDatabaseConstants.AUDIT_TRAIL_AUDIT);
        Assertions.assertEquals(1, auditTableVersionBefore, "Table version before migration");

        addStep("Ingest a entry to the database without the collection id", "works only in version 1.");
        String sqlInsert = "INSERT INTO " + AuditDatabaseConstants.FILE_TABLE +
                " ( " + AuditDatabaseConstants.FILE_FILE_ID + " ) VALUES ( ? )";
        DatabaseUtils.executeStatement(connector, sqlInsert, FILE_ID);

        addStep("Perform migration",
                "File table has version 2, audit table version 5 and database-version is 5");
        AuditTrailContributorDatabaseMigrator migrator = new AuditTrailContributorDatabaseMigrator(connector);
        migrator.migrate();
        int fileTableVersionAfter = DatabaseUtils.selectIntValue(connector, extractVersionSql,
                AuditDatabaseConstants.FILE_TABLE);
        Assertions.assertEquals(2, fileTableVersionAfter, "Table version after migration");
        int auditTableVersionAfter = DatabaseUtils.selectIntValue(connector, extractVersionSql,
                AuditDatabaseConstants.AUDIT_TRAIL_AUDIT);
        Assertions.assertEquals(5, auditTableVersionAfter, "Table version after migration");
        int dbTableVersionAfter = DatabaseUtils.selectIntValue(connector, extractVersionSql,
                AuditDatabaseConstants.DATABASE_VERSION_ENTRY);
        Assertions.assertEquals(AuditTrailContributorDatabaseMigrator.CURRENT_VERSION,
                dbTableVersionAfter, "Table version after migration");
    }
}
