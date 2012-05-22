package org.bitrepository.integrityservice.cache;

import org.bitrepository.integrityservice.cache.database.ChecksumState;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ChecksumStateTest extends ExtendedTestCase {
    
    @Test(groups = {"regressiontest"})
    public void testChecksumState() {
        addDescription("Test the checksum states.");
        addStep("Extract the checksum states.", "Should work.");
        ChecksumState[] states = ChecksumState.values();
        
        addStep("Check the order of checksum states", "Should be in same order as the Ordinal.");
        for(int i = 0; i < states.length; i++) {
            Assert.assertEquals(states[i], ChecksumState.fromOrdinal(i));
        }
    }
}
