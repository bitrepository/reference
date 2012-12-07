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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.TestIntegrityModel;
import org.bitrepository.integrityservice.checking.reports.MissingFileReportModel;
import org.bitrepository.integrityservice.mocks.MockIntegrityModel;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class RemoveDeletableFileIDsStepTest extends ExtendedTestCase {
    public static final String TEST_PILLAR_1 = "test-pillar-1";
    public static final List<String> PILLAR_IDS = Arrays.asList(TEST_PILLAR_1);
    public static final String TEST_FILE_1 = "test-file-1";
    public static final String DEFAULT_CHECKSUM = "0123456789";
    
    public static final Integer NUMBER_OF_PARTIAL_RESULTS = 3;
    
    protected Settings settings;
    
    @BeforeMethod (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings("IntegrityCheckingUnderTest");
        settings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        settings.getCollectionSettings().getClientSettings().getPillarIDs().addAll(PILLAR_IDS);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testNoFilesToDelete() {
        addDescription("Testing the case, when no files should be deleted from the database.");
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        MissingFileReportModel report = new MissingFileReportModel();
        WorkflowStep step = new RemoveDeletableFileIDsFromDatabase(store, report);
        Assert.assertEquals(store.getCallsForDeleteFileIdEntry(), 0);
        
        addStep("Perform the step of deleting file id entries based on the report.", 
                "No calls for deleting entries in the IntegrityModel.");
        step.performStep();
        Assert.assertEquals(store.getCallsForDeleteFileIdEntry(), 0);
    }
    
    @Test(groups = {"regressiontest", "integritytest"})
    public void testFileToDelete() {
        addDescription("Testing the case, when one file should be deleted from the database.");
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        store.addChecksums(createChecksumData(DEFAULT_CHECKSUM, TEST_FILE_1), TEST_PILLAR_1);
        MissingFileReportModel report = new MissingFileReportModel();
        report.reportDeletableFile(TEST_FILE_1);
        WorkflowStep step = new RemoveDeletableFileIDsFromDatabase(store, report);
        Assert.assertEquals(store.getCallsForDeleteFileIdEntry(), 0);
        
        addStep("Perform the step of deleting file id entries based on the report.", 
                "One calls for deleting the entry in the IntegrityModel.");
        step.performStep();
        Assert.assertEquals(store.getCallsForDeleteFileIdEntry(), 1);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testManyFilesToDelete() {
        addDescription("Testing the case, when 10 files should be deleted from the database.");
        MockIntegrityModel store = new MockIntegrityModel(new TestIntegrityModel(PILLAR_IDS));
        MissingFileReportModel report = new MissingFileReportModel();
        
        addStep("Populate the store and report with files.", "");
        int numberOfFiles = 10;
        for(int i = 0; i < numberOfFiles; i++) {
            String fileId = TEST_FILE_1 + "_" + i;
            store.addChecksums(createChecksumData(DEFAULT_CHECKSUM, fileId), TEST_PILLAR_1);
            report.reportDeletableFile(fileId);
        }
        WorkflowStep step = new RemoveDeletableFileIDsFromDatabase(store, report);
        Assert.assertEquals(store.getCallsForDeleteFileIdEntry(), 0);
        
        addStep("Perform the step of deleting file id entries based on the report.", 
                "One call for deleting each of the files in the IntegrityModel.");
        step.performStep();
        Assert.assertEquals(store.getCallsForDeleteFileIdEntry(), numberOfFiles);
    }

    private List<ChecksumDataForChecksumSpecTYPE> createChecksumData(String checksum, String ... fileids) {
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        for(String fileId : fileids) {
            ChecksumDataForChecksumSpecTYPE csData = new ChecksumDataForChecksumSpecTYPE();
            csData.setCalculationTimestamp(CalendarUtils.getNow());
            csData.setChecksumValue(Base16Utils.encodeBase16(checksum));
            csData.setFileID(fileId);
            res.add(csData);
        }
        return res;
    }
}
