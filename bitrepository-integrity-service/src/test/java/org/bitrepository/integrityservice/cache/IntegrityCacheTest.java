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

import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;

import static org.mockito.Mockito.*;

public class IntegrityCacheTest extends ExtendedTestCase {
    private IntegrityModel mockedDB;
    private static final String PILLAR1 = "pillar1";
    private static final String COLLECTION1 = "collection1";

    @BeforeMethod(alwaysRun = true)
    public void setup() throws Exception {
        mockedDB = mock(IntegrityModel.class);
    }

    @Test(groups = {"regressiontest"})
    public void numberOfMissingFileCachingTest() throws InterruptedException {
        addDescription("Tests the caching functionality related to number of missing files.");
        IntegrityCache cache = new IntegrityCache(mockedDB);
        addStep("Call getNumberOfMissingFiles the first time",
                "The the db should be used.");
        cache.getNumberOfMissingFiles(PILLAR1, COLLECTION1);
        verify(mockedDB, times(1)).getNumberOfMissingFiles(PILLAR1, COLLECTION1);

        addStep("Call getNumberOfMissingFiles again",
                "The cache value should be used, eg. the db should not be used.");
        cache.getNumberOfMissingFiles(PILLAR1, COLLECTION1);
        verify(mockedDB, times(1)).getNumberOfMissingFiles(PILLAR1, COLLECTION1);

        addStep("Called setFileMissing followed by a getNumberOfMissingFiles call",
                "The cache value should still be used as the refreshPeriodAfterDirtyMark hasn't been exceeded.");
        cache.setFileMissing("SomeFile", Arrays.asList(new String[]{PILLAR1}), COLLECTION1);
        cache.getNumberOfMissingFiles(PILLAR1, COLLECTION1);
        verify(mockedDB, times(1)).getNumberOfMissingFiles(PILLAR1, COLLECTION1);
    }

    @Test(groups = {"regressiontest"})
    public void numberOfFileCachingTest() throws InterruptedException {
        addDescription("Tests the caching functionality related to number of files.");
        IntegrityCache cache = new IntegrityCache(mockedDB);
        addStep("Call getNumberOfFiles the first time",
                "The the db should be used.");
        cache.getNumberOfFiles(PILLAR1, COLLECTION1);
        verify(mockedDB, times(1)).getNumberOfFiles(PILLAR1, COLLECTION1);

        addStep("Call getNumberOfFiles again",
                "The cache value should be used, eg. the db should not be used.");
        cache.getNumberOfFiles(PILLAR1, COLLECTION1);
        verify(mockedDB, times(1)).getNumberOfFiles(PILLAR1, COLLECTION1);

        addStep("Called setFile followed by a getNumberOfFiles call",
                "The cache value should still be used as the refreshPeriodAfterDirtyMark hasn't been exceeded.");
        cache.addFileIDs(null, PILLAR1, COLLECTION1);
        cache.getNumberOfFiles(PILLAR1, COLLECTION1);
        verify(mockedDB, times(1)).getNumberOfFiles(PILLAR1, COLLECTION1);
    }


    @Test(groups = {"regressiontest"})
    public void numberOfCorruptFileCachingTest() throws InterruptedException {
        addDescription("Tests the caching functionality related to number of files with inconsistent checksums.");
        IntegrityCache cache = new IntegrityCache(mockedDB);
        addStep("Call getNumberOfMissingFiles the first time",
                "The the db should be used.");
        cache.getNumberOfChecksumErrors(PILLAR1, COLLECTION1);
        verify(mockedDB, times(1)).getNumberOfChecksumErrors(PILLAR1, COLLECTION1);

        addStep("Call getNumberOfChecksumErrors again",
                "The cache value should be used, eg. the db should not be used.");
        cache.getNumberOfChecksumErrors(PILLAR1, COLLECTION1);
        verify(mockedDB, times(1)).getNumberOfChecksumErrors(PILLAR1, COLLECTION1);

        addStep("Called setFileMissing followed by a getNumberOfChecksumErrors call",
                "The cache value should still be used as the refreshPeriodAfterDirtyMark hasn't been exceeded.");
        cache.setChecksumError("SomeFile", Arrays.asList(new String[]{PILLAR1}), COLLECTION1);
        cache.getNumberOfChecksumErrors(PILLAR1, COLLECTION1);
        verify(mockedDB, times(1)).getNumberOfChecksumErrors(PILLAR1, COLLECTION1);

        addStep("Set refreshPeriodAfterDirtyMark to 1 second, called setChecksumError followed by a " +
                "getNumberOfChecksumErrors call",
                "The db value should be reread");
    }
}
