package org.bitrepository.integrityservice.cache;

import org.bitrepository.integrityservice.cache.database.FileState;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FileStateTest extends ExtendedTestCase {
    
    @Test(groups = {"regressiontest"})
    public void testFileState() {
        addDescription("Test the file states.");
        addStep("Extract the file states.", "Should work.");
        FileState[] states = FileState.values();
        
        addStep("Check the order of file states", "Should be in same order as the Ordinal.");
        for(int i = 0; i < states.length; i++) {
            Assert.assertEquals(states[i], FileState.fromOrdinal(i));
        }
    }
}
