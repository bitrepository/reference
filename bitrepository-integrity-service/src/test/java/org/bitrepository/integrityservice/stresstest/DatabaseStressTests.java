package org.bitrepository.integrityservice.stresstest;

import org.testng.annotations.Test;
import org.testng.AssertJUnit;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILE_INFO_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_TABLE;

import java.math.BigInteger;
import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO;
import org.bitrepository.integrityservice.cache.database.IntegrityDatabaseCreator;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

public class DatabaseStressTests extends ExtendedTestCase {
    
    private static final String PILLAR_1 = "pillar1";
    private static final String PILLAR_2 = "pillar2";
    private static final String PILLAR_3 = "pillar3";
    private static final String PILLAR_4 = "pillar4";
    
    private static final Integer NUMBER_OF_FILES = 10000;
    
    protected Settings settings;
    
    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");

        DerbyDatabaseDestroyer.deleteDatabase(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());

        IntegrityDatabaseCreator integrityDatabaseCreator = new IntegrityDatabaseCreator();
        integrityDatabaseCreator.createIntegrityDatabase(settings, null);
        
        settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR_1);
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR_2);
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR_3);
        settings.getCollectionSettings().getClientSettings().getPillarIDs().add(PILLAR_4);
        
        settings.getReferenceSettings().getIntegrityServiceSettings().setTimeBeforeMissingFileCheck(0);
    }
    
    protected void populateDatabase(IntegrityDAO cache) {
        FileIDsData data = new FileIDsData();
        FileIDsDataItems items = new FileIDsDataItems();
        XMLGregorianCalendar lastModificationTime = CalendarUtils.getNow();
        for(int i = 0; i < NUMBER_OF_FILES; i++) {
            FileIDsDataItem item = new FileIDsDataItem();
            item.setFileID("fileid-" + i);
            item.setFileSize(BigInteger.valueOf(i));
            item.setLastModificationTime(lastModificationTime);
            items.getFileIDsDataItem().add(item);
        }
        data.setFileIDsDataItems(items);
        cache.updateFileIDs(data, PILLAR_1);
        cache.updateFileIDs(data, PILLAR_2);
        cache.updateFileIDs(data, PILLAR_3);
        cache.updateFileIDs(data, PILLAR_4);
    }
    
    @AfterMethod (alwaysRun = true)
    public void clearDatabase() throws Exception {
        DBConnector connector = new DBConnector(settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        DatabaseUtils.executeStatement(connector, "DELETE FROM " + FILE_INFO_TABLE, new Object[0]);
        DatabaseUtils.executeStatement(connector, "DELETE FROM " + FILES_TABLE, new Object[0]);
        DatabaseUtils.executeStatement(connector, "DELETE FROM " + PILLAR_TABLE, new Object[0]);
    }
    
    @Test(groups = {"stresstest", "integritytest"})
    public void testDatabasePerformance() {
        addDescription("Testing the performance of the SQL queries to the database.");
        IntegrityDAO cache = createDAO();
        AssertJUnit.assertNotNull(cache);
        
        long startTime = System.currentTimeMillis();
        populateDatabase(cache);
        System.err.println("Time to ingest '" + NUMBER_OF_FILES + "' files: " + TimeUtils.millisecondsToHuman(System.currentTimeMillis() - startTime));
        
        startTime = System.currentTimeMillis();
        cache.setAllFileStatesToUnknown();
        System.err.println("Time to set all files to unknown: " + TimeUtils.millisecondsToHuman(System.currentTimeMillis() - startTime));
        
        startTime = System.currentTimeMillis();
        cache.setOldUnknownFilesToMissing(new Date());
        System.err.println("Time to set all files to missing: " + TimeUtils.millisecondsToHuman(System.currentTimeMillis() - startTime));
        
        startTime = System.currentTimeMillis();
        cache.findMissingFiles();
        System.err.println("Time to find missing files: " + TimeUtils.millisecondsToHuman(System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        cache.findMissingChecksums();
        System.err.println("Time to find missing checksums: " + TimeUtils.millisecondsToHuman(System.currentTimeMillis() - startTime));
    }
    
    private IntegrityDAO createDAO() {
        return new IntegrityDAO(new DBConnector(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase()),
                settings.getCollectionSettings().getClientSettings().getPillarIDs());
    }

}
