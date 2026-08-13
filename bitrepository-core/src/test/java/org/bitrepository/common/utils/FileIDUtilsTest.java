/*
 * #%L
 * Bitrepository Core
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
package org.bitrepository.common.utils;

import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

class FileIDUtilsTest {
    String FILE_ID = "Test-File-Id";

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void fileIDsTest() throws Exception {
        addDescription("Test the utility class for generating FileIDs");
        addStep("Test 'all file ids'", "is only AllFileIDs");
        FileIDs allFileIDs = FileIDsUtils.getAllFileIDs();
        Assertions.assertTrue(allFileIDs.isSetAllFileIDs());
        Assertions.assertFalse(allFileIDs.isSetFileID());
        Assertions.assertNull(allFileIDs.getFileID());

        addStep("Test a specific file id", "Should not be AllFileIDs");
        FileIDs specificFileIDs = FileIDsUtils.getSpecificFileIDs(FILE_ID);
        Assertions.assertFalse(specificFileIDs.isSetAllFileIDs());
        Assertions.assertTrue(specificFileIDs.isSetFileID());
        Assertions.assertEquals(FILE_ID, specificFileIDs.getFileID());
    }
}
