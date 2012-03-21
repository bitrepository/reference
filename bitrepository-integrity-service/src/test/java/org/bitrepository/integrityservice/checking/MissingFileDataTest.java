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
