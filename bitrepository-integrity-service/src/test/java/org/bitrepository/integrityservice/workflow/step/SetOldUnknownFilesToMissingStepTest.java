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
package org.bitrepository.integrityservice.workflow.step;

import java.math.BigInteger;
import java.util.Arrays;

import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.TestIntegrityModel;
import org.bitrepository.integrityservice.mocks.MockIntegrityModel;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class SetOldUnknownFilesToMissingStepTest extends ExtendedTestCase {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final String TEST_FILE_1 = "test-file-1";
    public static final int NUMBER_OF_FILES = 10;
    public static final String TEST_COLLECTION = "collection1";
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testUpdatingEmptyStore() {
        addDescription("Test the step for updating an empty integrity store.");
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(Arrays.asList(TEST_PILLAR_1)));
        SetOldUnknownFilesToMissingStep step = new SetOldUnknownFilesToMissingStep(store, TEST_COLLECTION);
        Assert.assertEquals(store.getCallsForSetAllFilesToUnknownFileState(), 0);
        Assert.assertEquals(store.getNumberOfFiles(TEST_PILLAR_1, TEST_COLLECTION), 0);
        Assert.assertEquals(store.getNumberOfMissingFiles(TEST_PILLAR_1, TEST_COLLECTION), 0);
        
        step.performStep();
        Assert.assertEquals(store.getCallsForSetUnknownFilesToMissing(), 1);
        Assert.assertEquals(store.getNumberOfFiles(TEST_PILLAR_1, TEST_COLLECTION), 0);
        Assert.assertEquals(store.getNumberOfMissingFiles(TEST_PILLAR_1, TEST_COLLECTION), 0);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testUpdatingExistingFiles() {
        addDescription("Testing the step when all the files are marked as existing.");
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(Arrays.asList(TEST_PILLAR_1)));
        for(int i = 0; i < NUMBER_OF_FILES; i++) {
            store.addFileIDs(getFileIDsData(TEST_FILE_1 + i), TEST_PILLAR_1, TEST_COLLECTION);
        }
        SetOldUnknownFilesToMissingStep step = new SetOldUnknownFilesToMissingStep(store, TEST_COLLECTION);
        step.performStep();
        Assert.assertEquals(store.getCallsForSetUnknownFilesToMissing(), 1);
        Assert.assertEquals(store.getNumberOfFiles(TEST_PILLAR_1, TEST_COLLECTION), 10);
        Assert.assertEquals(store.getNumberOfMissingFiles(TEST_PILLAR_1, TEST_COLLECTION), 0);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testUpdatingMissingFiles() {
        addDescription("Testing the step when all the files are marked as existing.");
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(Arrays.asList(TEST_PILLAR_1)));
        for(int i = 0; i < NUMBER_OF_FILES; i++) {
            store.addFileIDs(getFileIDsData(TEST_FILE_1 + i), TEST_PILLAR_1, TEST_COLLECTION);
            store.setFileMissing(TEST_FILE_1 + i, Arrays.asList(TEST_PILLAR_1), TEST_COLLECTION);
        }
        SetOldUnknownFilesToMissingStep step = new SetOldUnknownFilesToMissingStep(store, TEST_COLLECTION);
        step.performStep();
        Assert.assertEquals(store.getCallsForSetUnknownFilesToMissing(), 1);
        Assert.assertEquals(store.getNumberOfFiles(TEST_PILLAR_1, TEST_COLLECTION), 0);
        Assert.assertEquals(store.getNumberOfMissingFiles(TEST_PILLAR_1, TEST_COLLECTION), 10);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testUpdatingUnknownFiles() {
        addDescription("Testing the step when all the files are marked as unknown.");
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(Arrays.asList(TEST_PILLAR_1)));
        for(int i = 0; i < NUMBER_OF_FILES; i++) {
            store.addFileIDs(getFileIDsData(TEST_FILE_1 + i), TEST_PILLAR_1, TEST_COLLECTION);
        }
        store.setAllFilesToUnknownFileState(TEST_COLLECTION);
        
        SetOldUnknownFilesToMissingStep step = new SetOldUnknownFilesToMissingStep(store, TEST_COLLECTION);
        step.performStep();
        Assert.assertEquals(store.getCallsForSetUnknownFilesToMissing(), 1);
        Assert.assertEquals(store.getNumberOfFiles(TEST_PILLAR_1, TEST_COLLECTION), 0);
        Assert.assertEquals(store.getNumberOfMissingFiles(TEST_PILLAR_1, TEST_COLLECTION), 10);
    }
    
    private FileIDsData getFileIDsData(String... fileIds) {
        FileIDsData res = new FileIDsData();
        FileIDsDataItems items = new FileIDsDataItems();
        
        for(String fileId : fileIds) {
            FileIDsDataItem dataItem = new FileIDsDataItem();
            dataItem.setFileID(fileId);
            dataItem.setFileSize(BigInteger.valueOf(items.getFileIDsDataItem().size() + 1));
            dataItem.setLastModificationTime(CalendarUtils.getNow());
            items.getFileIDsDataItem().add(dataItem);
        } 
        
        res.setFileIDsDataItems(items);
        return res;
    }
}
