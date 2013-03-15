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

package org.bitrepository.integrityservice.cache;

import java.util.Arrays;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import static org.mockito.Mockito.*;

public class IntegrityCacheTest extends ExtendedTestCase {
    private IntegrityModel mockedDB;
    private static final String PILLAR1 = "pillar1";
    private static final String PILLAR2 = "pillar2";


    @BeforeMethod(alwaysRun = true)
    public void setup() throws Exception {
        mockedDB = mock(IntegrityModel.class);
    }

    @Test(groups = {"regressiontest"})
    public void numberOfMissingFileCachingTest() throws InterruptedException {
        IntegrityCache cache = new IntegrityCache(mockedDB);
        addStep("Call getNumberOfMissingFiles the first time",
                "The the db should be used.");
        cache.getNumberOfMissingFiles(PILLAR1);
        verify(mockedDB, times(1)).getNumberOfMissingFiles(PILLAR1);

        addStep("Call getNumberOfMissingFiles again",
                "The cache value should be used, eg. the db should not be used.");
        cache.getNumberOfMissingFiles(PILLAR1);
        verify(mockedDB, times(1)).getNumberOfMissingFiles(PILLAR1);

        addStep("Called setFileMissing followed by a getNumberOfMissingFiles call",
                "The cache value should still be used as the refreshPeriodAfterDirtyMark hasn't been exceeded.");
        cache.setFileMissing("SomeFile", Arrays.asList(new String[]{PILLAR1}));
        cache.getNumberOfMissingFiles(PILLAR1);
        verify(mockedDB, times(1)).getNumberOfMissingFiles(PILLAR1);

        addStep("Set refreshPeriodAfterDirtyMark to 1 second, called setFileMissing followed by a " +
                "getNumberOfMissingFiles call",
                "The db value should be reread");
        cache.refreshPeriodAfterDirtyMark = 1;
        cache.setFileMissing("SomeFile", Arrays.asList(new String[]{PILLAR1}));
        Thread.sleep(1001);
        cache.getNumberOfMissingFiles(PILLAR1);
        verify(mockedDB, times(2)).getNumberOfMissingFiles(PILLAR1);

        addStep("Call getNumberOfMissingFiles again",
                "The cache value should be used, eg. the db should not be used.");
        cache.getNumberOfMissingFiles(PILLAR1);
        verify(mockedDB, times(2)).getNumberOfMissingFiles(PILLAR1);

        addStep("Call setOldUnknownFilesToMissing and call getNumberOfMissingFiles again",
                "The db should be read for all pillars as the setOldUnknownFilesToMissing invalidates the .");
        reset(mockedDB);
        cache.setOldUnknownFilesToMissing();
        cache.getNumberOfMissingFiles(PILLAR1);
        cache.getNumberOfMissingFiles(PILLAR2);
        verify(mockedDB, times(1)).getNumberOfMissingFiles(PILLAR1);
        verify(mockedDB, times(1)).getNumberOfMissingFiles(PILLAR2);

        addStep("Call deleteFileIdEntry and call getNumberOfMissingFiles again",
                "The db should be read for all pillars as the deleteFileIdEntry invalidates the .");
        reset(mockedDB);
        cache.deleteFileIdEntry("");
        cache.getNumberOfMissingFiles(PILLAR1);
        cache.getNumberOfMissingFiles(PILLAR2);
        verify(mockedDB, times(1)).getNumberOfMissingFiles(PILLAR1);
        verify(mockedDB, times(1)).getNumberOfMissingFiles(PILLAR2);
    }

    @Test(groups = {"regressiontest"})
    public void numberOfFileCachingTest() throws InterruptedException {
        IntegrityCache cache = new IntegrityCache(mockedDB);
        addStep("Call getNumberOfFiles the first time",
                "The the db should be used.");
        cache.getNumberOfFiles(PILLAR1);
        verify(mockedDB, times(1)).getNumberOfFiles(PILLAR1);

        addStep("Call getNumberOfFiles again",
                "The cache value should be used, eg. the db should not be used.");
        cache.getNumberOfFiles(PILLAR1);
        verify(mockedDB, times(1)).getNumberOfFiles(PILLAR1);

        addStep("Called setFile followed by a getNumberOfFiles call",
                "The cache value should still be used as the refreshPeriodAfterDirtyMark hasn't been exceeded.");
        cache.addFileIDs(null, PILLAR1);
        cache.getNumberOfFiles(PILLAR1);
        verify(mockedDB, times(1)).getNumberOfFiles(PILLAR1);

        addStep("Set refreshPeriodAfterDirtyMark to 1 second, called setFile followed by a " +
                "getNumberOfFiles call",
                "The db value should be reread");
        cache.refreshPeriodAfterDirtyMark = 1;
        cache.addFileIDs(null, PILLAR1);
        Thread.sleep(1001);
        cache.getNumberOfFiles(PILLAR1);
        verify(mockedDB, times(2)).getNumberOfFiles(PILLAR1);

        addStep("Call getNumberOfFiles again",
                "The cache value should be used, eg. the db should not be used.");
        cache.getNumberOfFiles(PILLAR1);
        verify(mockedDB, times(2)).getNumberOfFiles(PILLAR1);

        addStep("Call deleteFileIdEntry and call getNumberOfFiles again",
                "The db should be read for all pillars as the deleteFileIdEntry invalidates the .");
        reset(mockedDB);
        cache.deleteFileIdEntry("");
        cache.getNumberOfFiles(PILLAR1);
        cache.getNumberOfFiles(PILLAR2);
        verify(mockedDB, times(1)).getNumberOfFiles(PILLAR1);
        verify(mockedDB, times(1)).getNumberOfFiles(PILLAR2);
    }


    @Test(groups = {"regressiontest"})
    public void numberOfCorruptFileCachingTest() throws InterruptedException {
        IntegrityCache cache = new IntegrityCache(mockedDB);
        addStep("Call getNumberOfMissingFiles the first time",
                "The the db should be used.");
        cache.getNumberOfChecksumErrors(PILLAR1);
        verify(mockedDB, times(1)).getNumberOfChecksumErrors(PILLAR1);

        addStep("Call getNumberOfChecksumErrors again",
                "The cache value should be used, eg. the db should not be used.");
        cache.getNumberOfChecksumErrors(PILLAR1);
        verify(mockedDB, times(1)).getNumberOfChecksumErrors(PILLAR1);

        addStep("Called setFileMissing followed by a getNumberOfChecksumErrors call",
                "The cache value should still be used as the refreshPeriodAfterDirtyMark hasn't been exceeded.");
        cache.setChecksumError("SomeFile", Arrays.asList(new String[]{PILLAR1}));
        cache.getNumberOfChecksumErrors(PILLAR1);
        verify(mockedDB, times(1)).getNumberOfChecksumErrors(PILLAR1);

        addStep("Set refreshPeriodAfterDirtyMark to 1 second, called setChecksumError followed by a " +
                "getNumberOfChecksumErrors call",
                "The db value should be reread");
        cache.refreshPeriodAfterDirtyMark = 1;
        cache.setChecksumError("SomeFile", Arrays.asList(new String[]{PILLAR1}));
        Thread.sleep(1001);
        cache.getNumberOfChecksumErrors(PILLAR1);
        verify(mockedDB, times(2)).getNumberOfChecksumErrors(PILLAR1);

        addStep("Called setChecksumAgreement followed by a getNumberOfChecksumErrors call",
                "The db value should be reread");
        cache.setChecksumAgreement("SomeFile", Arrays.asList(new String[]{PILLAR1}));
        Thread.sleep(1001);
        cache.getNumberOfChecksumErrors(PILLAR1);
        verify(mockedDB, times(3)).getNumberOfChecksumErrors(PILLAR1);

        addStep("Call getNumberOfChecksumErrors again",
                "The cache value should be used, eg. the db should not be used.");
        cache.getNumberOfChecksumErrors(PILLAR1);
        verify(mockedDB, times(3)).getNumberOfChecksumErrors(PILLAR1);

        addStep("Call setFilesWithConsistentChecksumToValid and call getNumberOfChecksumErrors again",
                "The db should be read for all pillars as the setOldUnknownFilesToMissing invalidates the .");
        reset(mockedDB);
        cache.setFilesWithConsistentChecksumToValid();
        cache.getNumberOfChecksumErrors(PILLAR1);
        verify(mockedDB, times(1)).getNumberOfChecksumErrors(PILLAR1);
        cache.getNumberOfChecksumErrors(PILLAR2);
        verify(mockedDB, times(1)).getNumberOfChecksumErrors(PILLAR2);

        addStep("Call setFilesWithConsistentChecksumToValid and call getNumberOfChecksumErrors again",
                "The db should be read for all pillars as the setOldUnknownFilesToMissing invalidates the .");
        reset(mockedDB);
        cache.setFilesWithConsistentChecksumToValid();
        cache.getNumberOfChecksumErrors(PILLAR1);
        verify(mockedDB, times(1)).getNumberOfChecksumErrors(PILLAR1);
        cache.getNumberOfChecksumErrors(PILLAR2);
        verify(mockedDB, times(1)).getNumberOfChecksumErrors(PILLAR2);

        addStep("Call deleteFileIdEntry and call getNumberOfChecksumErrors again",
                "The db should be read for all pillars as the deleteFileIdEntry invalidates the .");
        reset(mockedDB);
        cache.deleteFileIdEntry("");
        cache.getNumberOfChecksumErrors(PILLAR1);
        verify(mockedDB, times(1)).getNumberOfChecksumErrors(PILLAR1);
        cache.getNumberOfChecksumErrors(PILLAR2);
        verify(mockedDB, times(1)).getNumberOfChecksumErrors(PILLAR2);
    }

}
