/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.referencepillar.archive;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import org.bitrepository.common.filestore.DefaultFileInfo;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.cache.ChecksumDAO;
import org.bitrepository.pillar.cache.ChecksumStore;
import org.bitrepository.pillar.cache.MemoryCacheMock;
import org.bitrepository.pillar.cache.database.ExtractedChecksumResultSet;
import org.bitrepository.pillar.common.ChecksumDatabaseCreator;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.database.DerbyDatabaseDestroyer;
import org.bitrepository.settings.referencesettings.DatabaseSpecifics;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ReferenceChecksumManagerTest extends DefaultFixturePillarTest {
    private static final String TEST_FILE = "testFile"; 
    private static final String TEST_CONTENT = "This is the initial content."; 
    private static final String TEST_ADDED_CONTENT = "This is the newly added content to change the file."; 
    
    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest() throws Exception {
        File fileDir = new File(settingsForCUT.getReferenceSettings().getPillarSettings().getCollectionDirs().get(0).getFileDirs().get(0));
        if(fileDir.exists()) {
            FileUtils.delete(fileDir);
        }
    }

    //@Test( groups = {"regressiontest", "pillartest"})
    // Fails occasionally, see "BITMAG-801 - Fix ReferenceChecksumManagerTest.testRecalculationWithMockCache"
    public void testRecalculationWithMockCache() throws Exception {
        addDescription("Test the ability to recalculate the checksums automatically with a mock cache.");
        CollectionArchiveManager archives = new CollectionArchiveManager(settingsForCUT);
        ChecksumStore csCache = new MemoryCacheMock();
        AlarmDispatcher alarmDispatcher = new AlarmDispatcher(settingsForCUT, messageBus);
        ReferenceChecksumManager csManager =
                new ReferenceChecksumManager(archives, csCache, alarmDispatcher, settingsForCUT);

        testRecalculatingChecksumsDueToChangedFile(csManager, archives);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testRecalculationWithRealCache() throws Exception {
        addDescription("Test the ability to recalculate the checksums automatically with a database cache.");

        DatabaseSpecifics checksumDB =
                settingsForCUT.getReferenceSettings().getPillarSettings().getChecksumDatabase();
        DerbyDatabaseDestroyer.deleteDatabase(checksumDB);

        ChecksumDatabaseCreator checksumDatabaseCreator = new ChecksumDatabaseCreator();
        checksumDatabaseCreator.createChecksumDatabase(settingsForCUT, null);

        CollectionArchiveManager archives = new CollectionArchiveManager(settingsForCUT);
        ChecksumStore csCache = new ChecksumDAO(settingsForCUT);
        ReferenceChecksumManager csManager = new ReferenceChecksumManager(archives, csCache, 
                new AlarmDispatcher(settingsForCUT, messageBus), settingsForCUT);

        testRecalculatingChecksumsDueToChangedFile(csManager, archives);
    }
    
    private void testRecalculatingChecksumsDueToChangedFile(ReferenceChecksumManager csManager, CollectionArchiveManager archives) throws Exception {
        Date initialDate = new Date();
        ExtractedChecksumResultSet initialCsResults = csManager.getEntries(null, null, null, collectionID, 
        		ChecksumUtils.getDefault(settingsForCUT));
        Assert.assertEquals(initialCsResults.getEntries().size(), 0);

        synchronized(this) {
            addStep("The file system cannot handle timestamps in millis.", "One second wait, before inserting a file.");
            this.wait(1000);
        }
        
        addStep("Put a file into the archive", "The checksum of the file is extracted through the checksum entries");
        archives.downloadFileForValidation(TEST_FILE, collectionID, new ByteArrayInputStream(TEST_CONTENT.getBytes()));
        archives.moveToArchive(TEST_FILE, collectionID);
        ExtractedChecksumResultSet csResults = csManager.getEntries(null, null, null, collectionID, 
        		ChecksumUtils.getDefault(settingsForCUT));
        Assert.assertEquals(csResults.getEntries().size(), 1);
        
        addStep("Validate the dates", "Calculation time should be between the initial test time and now.");
        Date beforeChange = new Date();
        Assert.assertTrue(CalendarUtils.convertFromXMLGregorianCalendar(
                csResults.getEntries().get(0).getCalculationTimestamp()).getTime() > initialDate.getTime());
        Assert.assertTrue(CalendarUtils.convertFromXMLGregorianCalendar(
                csResults.getEntries().get(0).getCalculationTimestamp()).getTime() < beforeChange.getTime());
        Assert.assertTrue(archives.getFileInfo(TEST_FILE, collectionID).getMdate() >= initialDate.getTime()); 
        Assert.assertTrue(archives.getFileInfo(TEST_FILE, collectionID).getMdate() <= beforeChange.getTime());

        synchronized(this) {
            addStep("The file system cannot handle timestamps in millis.", "One second wait, before changing the file.");
            this.wait(1000);
        }

        addStep("Change the file", "Should be recalculated and thus have a newer timestamp");
        DefaultFileInfo dfi = (DefaultFileInfo) archives.getFileInfo(TEST_FILE, collectionID);
        FileOutputStream out = new FileOutputStream(dfi.getFile(), true);
        out.write("\n".getBytes());
        out.write(TEST_ADDED_CONTENT.getBytes());
        out.flush();
        out.close();

        ExtractedChecksumResultSet changedCsResults = csManager.getEntries(null, null, null, collectionID, 
        		ChecksumUtils.getDefault(settingsForCUT));
        Assert.assertEquals(changedCsResults.getEntries().size(), 1);
        
        addStep("Validate the dates", "New calculation time should be between the initial test time and now.");
        Date afterChange = new Date();
        Assert.assertTrue(CalendarUtils.convertFromXMLGregorianCalendar(
                changedCsResults.getEntries().get(0).getCalculationTimestamp()).getTime() > initialDate.getTime());
        Assert.assertTrue(CalendarUtils.convertFromXMLGregorianCalendar(
                changedCsResults.getEntries().get(0).getCalculationTimestamp()).getTime() > beforeChange.getTime());
        Assert.assertTrue(CalendarUtils.convertFromXMLGregorianCalendar(
                changedCsResults.getEntries().get(0).getCalculationTimestamp()).getTime() <= afterChange.getTime());
        Assert.assertTrue(archives.getFileInfo(TEST_FILE, collectionID).getMdate() >= initialDate.getTime());
        Assert.assertTrue(archives.getFileInfo(TEST_FILE, collectionID).getMdate() >= beforeChange.getTime());
        Assert.assertTrue(archives.getFileInfo(TEST_FILE, collectionID).getMdate() <= afterChange.getTime());
        
        addStep("Test the checksum of before and after the change", "Not identical");
        String csBefore = Base16Utils.decodeBase16(csResults.getEntries().get(0).getChecksumValue());
        String csAfter = Base16Utils.decodeBase16(changedCsResults.getEntries().get(0).getChecksumValue());
        Assert.assertFalse(csBefore.equals(csAfter));
    }
}
