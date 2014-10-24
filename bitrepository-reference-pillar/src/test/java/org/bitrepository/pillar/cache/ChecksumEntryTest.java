package org.bitrepository.pillar.cache;
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

import java.util.Date;

import org.bitrepository.pillar.store.checksumdatabase.ChecksumEntry;
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
