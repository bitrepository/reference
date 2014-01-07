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

        import java.util.Arrays;
import java.util.Date;
import java.util.LinkedList;
import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.cache.ChecksumEntry;
import org.bitrepository.pillar.cache.ChecksumStore;
import org.bitrepository.pillar.cache.database.ExtractedChecksumResultSet;
import org.bitrepository.service.AlarmDispatcher;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.AssertJUnit.assertTrue;

public class ReferenceChecksumManagerTest extends ExtendedTestCase {
    private static final String DEFAULT_COLlECTION_ID = "defaultTestCollection";
    private static final String COMPONENTID = "ChecksumManagerUnderTest";

    private AlarmDispatcher alarmDispatcher;
    private CollectionArchiveManager archiveManager;
    private ChecksumStore cache;
    private Settings settings = TestSettingsProvider.getSettings(COMPONENTID);
    private ChecksumSpecTYPE defaultChecksumType;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest() throws Exception {
        alarmDispatcher = mock(AlarmDispatcher.class);
        archiveManager = mock(CollectionArchiveManager.class);
        cache = mock(ChecksumStore.class);
        settings = TestSettingsProvider.getSettings("");
        defaultChecksumType = ChecksumUtils.getDefault(settings);
    }

    @Test( groups = {"regressiontest"})
    public void testEmptyArchive() {
        addDescription("Test the ChecksumManager on a empty archive");

        addStep("Call the getEntries method where the cache and archive returns a empty list of files/checksums.",
                "The call should be delegated to the checksum cache, and the result from here should nbe returned. ");
        ReferenceChecksumManager checksumManagerUnderTestManager = new ReferenceChecksumManager(
                archiveManager, cache, alarmDispatcher, settings);
        when(cache.getAllFileIDs(DEFAULT_COLlECTION_ID)).thenReturn(new LinkedList<String>());
        when(archiveManager.getAllFileIds(DEFAULT_COLlECTION_ID)).thenReturn(new LinkedList<String>());
        XMLGregorianCalendar minTimeStamp = CalendarUtils.getXmlGregorianCalendar(new Date());
        XMLGregorianCalendar maxTimeStamp = CalendarUtils.getXmlGregorianCalendar(new Date());
        long maxNumberOfResults = 10;
        ExtractedChecksumResultSet expectedCsResults = new ExtractedChecksumResultSet();
        when(cache.getEntries(minTimeStamp, maxTimeStamp, maxNumberOfResults, DEFAULT_COLlECTION_ID)).thenReturn(
                expectedCsResults);

        ExtractedChecksumResultSet receivedCsResults = checksumManagerUnderTestManager.getEntries(
                minTimeStamp, maxTimeStamp, maxNumberOfResults, DEFAULT_COLlECTION_ID, defaultChecksumType);
        assertTrue(expectedCsResults == receivedCsResults);
    }

    @Test( groups = {"regressiontest"})
    public void testChecksumRecalculation() {
        addDescription("Verifies that the checksum is recalculated if the files has changed on disk, eg. the modified" +
                "time stamp of the files has changed.");

        addStep("Call the getEntries method and return a checksum timestamp from the cache older than the archived file " +
                "modification date.", "A call should be made to the recalculateChecksum for the concrete fileID.");
        TestReferenceChecksumManager checksumManagerUnderTestManager = new TestReferenceChecksumManager(
                archiveManager, cache, alarmDispatcher, settings);

        addStep("Change the file", "Should be recalculated and thus have a newer timestamp");
        String fileId = "testFile";
        when(archiveManager.getAllFileIds(DEFAULT_COLlECTION_ID)).thenReturn(Arrays.asList(new String[] {fileId}));
        when(cache.hasFile(fileId, DEFAULT_COLlECTION_ID)).thenReturn(true);
        ExtractedChecksumResultSet expectedCsResults = new ExtractedChecksumResultSet();
        ChecksumEntry cs = new ChecksumEntry(fileId, "aa", new Date(System.currentTimeMillis()-10));
        expectedCsResults.insertChecksumEntry(cs);
        when(cache.getEntries(null, null, null, DEFAULT_COLlECTION_ID)).thenReturn(expectedCsResults);
        when(cache.getCalculationDate(fileId, DEFAULT_COLlECTION_ID)).thenReturn(new Date());
        FileInfo fileInfoMock = mock(FileInfo.class);
        when(fileInfoMock.getMdate()).thenReturn(new Date().getTime());
        when(archiveManager.getFileInfo(fileId,DEFAULT_COLlECTION_ID)).thenReturn(fileInfoMock);
        checksumManagerUnderTestManager.getEntries(
                null, null, null, DEFAULT_COLlECTION_ID, defaultChecksumType);
        assertTrue(checksumManagerUnderTestManager.recalculateCheckSumCalled);
    }

    class TestReferenceChecksumManager extends ReferenceChecksumManager {
        boolean recalculateCheckSumCalled = false;
        public TestReferenceChecksumManager(FileStore archives, ChecksumStore cache, AlarmDispatcher alarmDispatcher, Settings settings) {
            super(archives, cache, alarmDispatcher, settings);
        }

        @Override
        public void recalculateChecksum(String fileId, String collectionId) {
            recalculateCheckSumCalled = true;
        }
    }
}
