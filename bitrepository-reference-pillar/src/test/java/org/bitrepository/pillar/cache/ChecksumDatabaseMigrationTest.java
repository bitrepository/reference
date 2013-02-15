package org.bitrepository.pillar.cache;

import static org.bitrepository.pillar.cache.database.DatabaseConstants.CHECKSUM_TABLE;
import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_CHECKSUM;
import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_COLLECTION_ID;
import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_DATE;
import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_FILE_ID;

import java.io.File;
import java.util.Date;

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

public class ChecksumDatabaseMigrationTest extends ExtendedTestCase {
    protected Settings settings;
    
    static final String PATH_TO_DATABASE_UNPACKED = "target/test/referencepillar/checksumdb-version1";
    static final String PATH_TO_DATABASE_JAR_FILE = "src/test/resources/checksumdb-version1.jar";
    
    static final String FILE_ID = "default-file-id";
    static final String CHECKSUM = "default-checksum";

    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("ReferencePillarTest");
        
        settings.getReferenceSettings().getPillarSettings().getChecksumDatabase().setDatabaseURL(
                "jdbc:derby:" + PATH_TO_DATABASE_UNPACKED + "/checksumdb");

        DatabaseSpecifics checksumDB =
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase();
        DerbyDatabaseDestroyer.deleteDatabase(checksumDB);

        FileUtils.unzip(new File(PATH_TO_DATABASE_JAR_FILE), FileUtils.retrieveDirectory(PATH_TO_DATABASE_UNPACKED));
    }
    
    @AfterMethod (alwaysRun = true)
    public void cleanup() throws Exception {
        FileUtils.deleteDirIfExists(new File(PATH_TO_DATABASE_UNPACKED));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testMigratingDatabaseChecksumsTable() {
        addDescription("Tests that the checksums table can be migrated from version 1 to 2, e.g. getting the column "
                + "collectionid, which should be set to the default in settings.");
        DBConnector connector = new DBConnector(
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase());

        addStep("Validate setup", "Checksums table has version 1");
        String extractVersionSql = "SELECT version FROM tableversions WHERE tablename = ?";
        int versionBefore = DatabaseUtils.selectIntValue(connector, extractVersionSql, "checksums");
        Assert.assertEquals(versionBefore, 1, "Table version before migration");
        
        addStep("Ingest a entry to the database without the collection id", "works only in version 1.");
        String insertSql = "INSERT INTO " + CHECKSUM_TABLE + " ( " + CS_FILE_ID + " , " + CS_CHECKSUM + " , " + CS_DATE 
                + " ) VALUES ( ? , ? , ? )";
        DatabaseUtils.executeStatement(connector, insertSql, FILE_ID, CHECKSUM, new Date());
        
        addStep("Perform migration", "Checksums table has version 2");
        ChecksumDBMigrator migrator = new ChecksumDBMigrator(connector, settings);
        migrator.migrate();
        int versionAfter = DatabaseUtils.selectIntValue(connector, extractVersionSql, "checksums");
        Assert.assertEquals(versionAfter, 2, "Table version after migration");
        
        addStep("Validate the entry", "The collection id has been set to the default collection id");
        String retrieveCollectionIdSql = "SELECT " + CS_COLLECTION_ID + " FROM " + CHECKSUM_TABLE + " WHERE " 
                + CS_FILE_ID + " = ?";
        String collectionId = DatabaseUtils.selectStringValue(connector, retrieveCollectionIdSql, FILE_ID);
        Assert.assertEquals(collectionId, settings.getCollectionID());
    }
}
