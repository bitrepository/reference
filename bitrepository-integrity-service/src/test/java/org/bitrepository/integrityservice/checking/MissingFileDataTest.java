package org.bitrepository.integrityservice.checking;

import java.util.Arrays;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class MissingFileDataTest extends ExtendedTestCase {
    
    private static final String MISSING_FILE = "missing-file";
    private static final String PILLAR_ID = "pillar-id";
    
    @Test(groups = {"regressiontest"})
    public void testMissingFileData() {
        addDescription("Testing the functionality of the MissingFileData.");
        addStep("Create and populate the missing file data", "Not errors");
        MissingFileData fileData = new MissingFileData(MISSING_FILE, Arrays.asList(PILLAR_ID));
        
        addStep("Validate the content of the MissingFileData", "Should be the input data.");
        Assert.assertEquals(fileData.getFileId(), MISSING_FILE);
        Assert.assertEquals(fileData.getPillarIds().size(), 1, "Should have one pillar id");
        Assert.assertEquals(fileData.getPillarIds().toArray()[0], PILLAR_ID, "Should have one pillar id");
        Assert.assertTrue(fileData.toString().contains(PILLAR_ID));
        Assert.assertTrue(fileData.toString().contains(MISSING_FILE));
    }
}
