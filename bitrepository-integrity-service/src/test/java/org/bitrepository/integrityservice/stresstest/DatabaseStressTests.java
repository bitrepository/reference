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
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.integrityservice.cache.IntegrityDatabaseManager;
import org.bitrepository.integrityservice.cache.database.DerbyIntegrityDAO;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO;
import org.bitrepository.integrityservice.cache.database.IntegrityDatabaseCreator;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseManager;
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
        
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().clear();
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_1);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_2);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_3);
        settings.getRepositorySettings().getCollections().getCollection().get(0).getPillarIDs().getPillarID().add(PILLAR_4);
        
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
        String collectionID = settings.getRepositorySettings().getCollections().getCollection().get(0).getID();
        cache.updateFileIDs(data, PILLAR_1, collectionID);
        cache.updateFileIDs(data, PILLAR_2, collectionID);
        cache.updateFileIDs(data, PILLAR_3, collectionID);
        cache.updateFileIDs(data, PILLAR_4, collectionID);
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
        cache.setExistingFilesToPreviouslySeenFileState(settings.getRepositorySettings().getCollections().getCollection().get(0).getID());
        System.err.println("Time to set all files to unknown: " + TimeUtils.millisecondsToHuman(System.currentTimeMillis() - startTime));
        
        startTime = System.currentTimeMillis();
        cache.setOldUnknownFilesToMissing(new Date(), settings.getRepositorySettings().getCollections().getCollection().get(0).getID());
        System.err.println("Time to set all files to missing: " + TimeUtils.millisecondsToHuman(System.currentTimeMillis() - startTime));
        
        startTime = System.currentTimeMillis();
        cache.findMissingFiles(settings.getRepositorySettings().getCollections().getCollection().get(0).getID());
        System.err.println("Time to find missing files: " + TimeUtils.millisecondsToHuman(System.currentTimeMillis() - startTime));

        startTime = System.currentTimeMillis();
        cache.findMissingChecksums(settings.getRepositorySettings().getCollections().getCollection().get(0).getID());
        System.err.println("Time to find missing checksums: " + TimeUtils.millisecondsToHuman(System.currentTimeMillis() - startTime));
    }
    
    private IntegrityDAO createDAO() {
        DatabaseManager dm = new IntegrityDatabaseManager(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
        return new DerbyIntegrityDAO(dm, settings.getRepositorySettings().getCollections());
    }

}
