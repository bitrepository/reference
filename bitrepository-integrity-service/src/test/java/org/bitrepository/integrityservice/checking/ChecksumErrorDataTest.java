package org.bitrepository.integrityservice.checking;

import java.util.Arrays;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ChecksumErrorDataTest extends ExtendedTestCase {
    
    private static final String MISSING_FILE = "missing-file";
    private static final String PILLAR_ID = "pillar-id";
    
    @Test(groups = {"regressiontest"})
    public void testChecksumErrorData() {
        addDescription("Testing the functionality of the ChecksumErrorData.");
        addStep("Create and populate the checksum error data", "Not errors");
        ChecksumErrorData fileData = new ChecksumErrorData(MISSING_FILE, Arrays.asList(PILLAR_ID));
        
        addStep("Validate the content of the MissingFileData", "Should be the input data.");
        Assert.assertEquals(fileData.getFileId(), MISSING_FILE);
        Assert.assertEquals(fileData.getPillarIds().size(), 1, "Should have one pillar id");
        Assert.assertEquals(fileData.getPillarIds().toArray()[0], PILLAR_ID, "Should have one pillar id");
        Assert.assertTrue(fileData.toString().contains(PILLAR_ID));
        Assert.assertTrue(fileData.toString().contains(MISSING_FILE));
    }
}
