package org.bitrepository.audittrails.store;

import java.sql.Connection;
import java.util.Date;

import org.bitrepository.common.database.DatabaseUtils;
import org.bitrepository.common.database.DerbyDBConnector;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AuditDatabaseTester extends ExtendedTestCase{
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    String DATABASE_URL = "jdbc:derby:auditservicedb";
    
    @BeforeClass (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings();
    }
    
    @Test(groups = {"databasetest"})
    public void defaultAlarmHandlingTest() throws Exception {
        addDescription("Testing the connection to the integrity database.");
        addStep("Setup the variables and constants.", "Should be ok.");
        
        String fileId = "TEST-FILE-ID-" + new Date().getTime();
        String fileId2 = "CONSISTEN-FILE-ID";
        String pillarId = "MY-TEST-PILLAR";
        
        addStep("Adds the variables to the settings and instantaites the database cache", "Should be connected.");
        settings.getReferenceSettings().getIntegrityServiceSettings().setDatabaseUrl(DATABASE_URL);
        AuditTrailServiceDAO database = new AuditTrailServiceDAO(settings);
        clearDatabase(DATABASE_URL);

        
    }
    

    private void clearDatabase(String url) throws Exception {
        Connection con = new DerbyDBConnector().getEmbeddedDBConnection(url);
        
        String sqlFI = "DELETE FROM " + AuditDatabaseConstants.FILE_TABLE;
        DatabaseUtils.executeStatement(con, sqlFI, new Object[0]);
        String sqlFiles = "DELETE FROM " + AuditDatabaseConstants.CONTRIBUTOR_TABLE;
        DatabaseUtils.executeStatement(con, sqlFiles, new Object[0]);
        String sqlPillar = "DELETE FROM " + AuditDatabaseConstants.AUDITTRAIL_TABLE;
        DatabaseUtils.executeStatement(con, sqlPillar, new Object[0]);
        String sqlCs = "DELETE FROM " + AuditDatabaseConstants.ACTOR_TABLE;
        DatabaseUtils.executeStatement(con, sqlCs, new Object[0]);
    }
}
