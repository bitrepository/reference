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
package org.bitrepository.pillar.integration.func.getfileids;

import java.util.List;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.client.exceptions.NegativeResponseException;
import org.bitrepository.pillar.integration.func.PillarFunctionTest;
import org.testng.Assert;
import org.testng.annotations.Test;

public class GetFileIDsTest extends PillarFunctionTest {

    @Test ( groups = {"pillar-integration-test"} )
    public void testRetrievalOfAllFileIDs() throws NegativeResponseException {
        addDescription("Test the pillar is able to deliver all file ids.");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);

        addStep("Request the id for all files on the pillar",
            "A list (at least 2 long) of file ids should be returned.");
        List<FileIDsDataItem> fileids = pillarFileManager.getFileIDs(null);
        Assert.assertTrue(fileids.size() >= 2, "The length of the returned fileids were less that 2");
    }
}
