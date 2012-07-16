package org.bitrepository.integrityservice;

import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILE_INFO_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_TABLE;

import java.io.File;
import java.sql.Connection;

import org.bitrepository.common.database.DBConnector;
import org.bitrepository.common.database.DatabaseUtils;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.DatabaseTestUtils;
import org.bitrepository.common.utils.FileUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class IntegrityDatabaseTestCase extends ExtendedTestCase {
    protected Settings settings;

    protected final String DATABASE_NAME = "integritydb";
    protected final String DATABASE_DIRECTORY = "test-database";
    protected final String DATABASE_URL = "jdbc:derby:" + DATABASE_DIRECTORY + "/" + DATABASE_NAME;
    
    private File dbDir = null;
    private Connection dbCon;
    
    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");
        
        File dbFile = new File("src/test/resources/integritydb.jar");
        Assert.assertTrue(dbFile.isFile(), "The database file should exist");
        
        dbDir = FileUtils.retrieveDirectory(DATABASE_DIRECTORY);
        FileUtils.retrieveSubDirectory(dbDir, DATABASE_NAME);
        
        dbCon = DatabaseTestUtils.takeDatabase(dbFile, DATABASE_NAME, dbDir);
        dbCon.close();
        settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase().setDatabaseURL(DATABASE_URL);
    }
    
    @AfterMethod (alwaysRun = true)
    public void clearDatabase() throws Exception {
        DBConnector connector = new DBConnector(settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        DatabaseUtils.executeStatement(connector, "DELETE FROM " + FILE_INFO_TABLE, new Object[0]);
        DatabaseUtils.executeStatement(connector, "DELETE FROM " + FILES_TABLE, new Object[0]);
        DatabaseUtils.executeStatement(connector, "DELETE FROM " + PILLAR_TABLE, new Object[0]);
    }

    @AfterClass (alwaysRun = true)
    public void cleanup() throws Exception {
        if(dbCon != null) {
            dbCon.close();
        }
        if(dbDir != null) {
            FileUtils.delete(dbDir);
        }
    }
}
