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
