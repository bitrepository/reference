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
package org.bitrepository.pillar.referencepillar;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;

import org.apache.kahadb.util.ByteArrayInputStream;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.cache.ChecksumDAO;
import org.bitrepository.pillar.cache.ChecksumStore;
import org.bitrepository.pillar.cache.MemoryCache;
import org.bitrepository.pillar.cache.database.ExtractedChecksumResultSet;
import org.bitrepository.pillar.common.ChecksumDatabaseCreator;
import org.bitrepository.pillar.referencepillar.archive.ReferenceArchive;
import org.bitrepository.pillar.referencepillar.archive.ReferenceChecksumManager;
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
        File fileDir = new File(settingsForCUT.getReferenceSettings().getPillarSettings().getFileDir().get(0));
        if(fileDir.exists()) {
            FileUtils.delete(fileDir);
        }
    }

    //@Test( groups = {"regressiontest", "pillartest"})
    // Fails occasionally, see "BITMAG-801 - Fix ReferenceChecksumManagerTest.testRecalculationWithMockCache"
    public void testRecalculationWithMockCache() throws Exception {
        addDescription("Test the ability to recalculate the checksums automatically with a mock cache.");
        ReferenceArchive archive =
                new ReferenceArchive(settingsForCUT.getReferenceSettings().getPillarSettings().getFileDir());
        ChecksumStore csCache = new MemoryCache();
        AlarmDispatcher alarmDispatcher = new AlarmDispatcher(settingsForCUT, messageBus);
        ReferenceChecksumManager csManager =
                new ReferenceChecksumManager(archive, csCache, alarmDispatcher, ChecksumUtils.getDefault(settingsForCUT), 3600000L);

        testRecalculatingChecksumsDueToChangedFile(csManager, archive);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testRecalculationWithRealCache() throws Exception {
        addDescription("Test the ability to recalculate the checksums automatically with a database cache.");
        Settings settings = TestSettingsProvider.reloadSettings("ReferencePillarTest");

        DatabaseSpecifics checksumDB =
                settings.getReferenceSettings().getPillarSettings().getChecksumDatabase();
        DerbyDatabaseDestroyer.deleteDatabase(checksumDB);

        ChecksumDatabaseCreator checksumDatabaseCreator = new ChecksumDatabaseCreator();
        checksumDatabaseCreator.createChecksumDatabase(settings, null);

        ReferenceArchive archive = new ReferenceArchive(settings.getReferenceSettings().getPillarSettings().getFileDir());
        ChecksumStore csCache = new ChecksumDAO(settings);
        ReferenceChecksumManager csManager = new ReferenceChecksumManager(archive, csCache, 
                new AlarmDispatcher(settingsForCUT, messageBus), ChecksumUtils.getDefault(settingsForCUT), 3600000L);

        testRecalculatingChecksumsDueToChangedFile(csManager, archive);
    }
    
    private void testRecalculatingChecksumsDueToChangedFile(ReferenceChecksumManager csManager, ReferenceArchive archive) throws Exception {
        Date initialDate = new Date();
        ExtractedChecksumResultSet initialCsResults = csManager.getEntries(null, null, null);
        Assert.assertEquals(initialCsResults.getEntries().size(), 0);

        synchronized(this) {
            addStep("The file system cannot handle timestamps in millis.", "One second wait, before inserting a file.");
            this.wait(1000);
        }
        
        addStep("Put a file into the archive", "The checksum of the file is extracted through the checksum entries");
        archive.downloadFileForValidation(TEST_FILE, new ByteArrayInputStream(TEST_CONTENT.getBytes()));
        archive.moveToArchive(TEST_FILE);
        ExtractedChecksumResultSet csResults = csManager.getEntries(null, null, null);
        Assert.assertEquals(csResults.getEntries().size(), 1);
        
        addStep("Validate the dates", "Calculation time should be between the initial test time and now.");
        Date beforeChange = new Date();
        Assert.assertTrue(CalendarUtils.convertFromXMLGregorianCalendar(
                csResults.getEntries().get(0).getCalculationTimestamp()).getTime() > initialDate.getTime());
        Assert.assertTrue(CalendarUtils.convertFromXMLGregorianCalendar(
                csResults.getEntries().get(0).getCalculationTimestamp()).getTime() < beforeChange.getTime());
        Assert.assertTrue(archive.getFile(TEST_FILE).lastModified() >= initialDate.getTime()); 
        Assert.assertTrue(archive.getFile(TEST_FILE).lastModified() <= beforeChange.getTime());

        synchronized(this) {
            addStep("The file system cannot handle timestamps in millis.", "One second wait, before changing the file.");
            this.wait(1000);
        }

        addStep("Change the file", "Should be recalculated and thus have a newer timestamp");
        FileOutputStream out = new FileOutputStream(archive.getFile(TEST_FILE), true);
        out.write("\n".getBytes());
        out.write(TEST_ADDED_CONTENT.getBytes());
        out.flush();
        out.close();

        ExtractedChecksumResultSet changedCsResults = csManager.getEntries(null, null, null);
        Assert.assertEquals(changedCsResults.getEntries().size(), 1);
        
        addStep("Validate the dates", "New calculation time should be between the initial test time and now.");
        Date afterChange = new Date();
        Assert.assertTrue(CalendarUtils.convertFromXMLGregorianCalendar(
                changedCsResults.getEntries().get(0).getCalculationTimestamp()).getTime() > initialDate.getTime());
        Assert.assertTrue(CalendarUtils.convertFromXMLGregorianCalendar(
                changedCsResults.getEntries().get(0).getCalculationTimestamp()).getTime() > beforeChange.getTime());
        Assert.assertTrue(CalendarUtils.convertFromXMLGregorianCalendar(
                changedCsResults.getEntries().get(0).getCalculationTimestamp()).getTime() <= afterChange.getTime());
        Assert.assertTrue(archive.getFile(TEST_FILE).lastModified() >= initialDate.getTime());
        Assert.assertTrue(archive.getFile(TEST_FILE).lastModified() >= beforeChange.getTime());
        Assert.assertTrue(archive.getFile(TEST_FILE).lastModified() <= afterChange.getTime());
        
        addStep("Test the checksum of before and after the change", "Not identical");
        String csBefore = Base16Utils.decodeBase16(csResults.getEntries().get(0).getChecksumValue());
        String csAfter = Base16Utils.decodeBase16(changedCsResults.getEntries().get(0).getChecksumValue());
        Assert.assertFalse(csBefore.equals(csAfter));
    }
}
