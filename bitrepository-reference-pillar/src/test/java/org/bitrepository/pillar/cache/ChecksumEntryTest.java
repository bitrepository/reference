package org.bitrepository.pillar.cache;

import java.util.Date;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class ChecksumEntryTest extends ExtendedTestCase {
    private static final String CE_FILE = "file";
    private static final String CE_CHECKSUM = "checksum";
    private static final Date CE_DATE = new Date(1234567890);
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testExtendedTestCase() throws Exception {
        addDescription("Test the ChecksumEntry");
        addStep("Create a ChecksumEntry", "The data should be extractable again.");
        ChecksumEntry ce = new ChecksumEntry(CE_FILE, CE_CHECKSUM, CE_DATE);
        Assert.assertEquals(ce.getFileId(), CE_FILE);
        Assert.assertEquals(ce.getChecksum(), CE_CHECKSUM);
        Assert.assertEquals(ce.getCalculationDate(), CE_DATE);
        
        addStep("Change the value of the checksum", "The new checksum should be extracted.");
        String newChecksum = "newChecksum" + new Date().getTime();
        ce.setChecksum(newChecksum);
        Assert.assertEquals(ce.getChecksum(), newChecksum);
        
        addStep("Change the value of the calculation date", "The new date should be extracted.");
        Date newDate = new Date(9876543210l);
        ce.setCalculationDate(newDate);
        Assert.assertEquals(ce.getCalculationDate(), newDate);
    }
}
