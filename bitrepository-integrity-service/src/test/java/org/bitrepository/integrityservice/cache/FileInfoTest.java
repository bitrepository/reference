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

import org.bitrepository.TestGroups;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

class FileInfoTest {

    private static final String FILE_ID = "TEST-FILE";
    private static final long LAST_FILE_CHECK_MILLIS = 1000000L;
    private static final Instant LAST_FILE_CHECK = Instant.ofEpochMilli(LAST_FILE_CHECK_MILLIS);
    private static final String CHECKSUM = "CHECKSUM";
    private static final long LAST_CHECKSUM_CHECK_MILLIS = 2000000L;
    private static final Instant LAST_CHECKSUM_CHECK = Instant.ofEpochMilli(LAST_CHECKSUM_CHECK_MILLIS);
    private static final String PILLAR_ID = "test-pillar";
    private static final Long FILE_SIZE = 12345L;

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    @Tag("integritytest")
    void testFileInfo() {
        addDescription("Tests the FileInfo element. Adds all data and extracts it again.");
        addStep("Setup the file info.", "Should be possible to extract all the data again.");
        FileInfo fi = new FileInfo(FILE_ID, LAST_FILE_CHECK, CHECKSUM, FILE_SIZE, LAST_CHECKSUM_CHECK, PILLAR_ID, null, null);

        assertEquals(FILE_ID, fi.getFileId());
        assertEquals(LAST_FILE_CHECK, fi.getDateForLastFileIDCheckInstant());
        assertEquals(CHECKSUM, fi.getChecksum());
        assertEquals(LAST_CHECKSUM_CHECK, fi.getDateForLastChecksumCheckInstant());
        assertEquals(PILLAR_ID, fi.getPillarId());
        assertEquals(FILE_SIZE, fi.getFileSize());

        addStep("Change the checksum", "Should be possible to extract it again.");
        String newChecksum = "NEW-CHECKSUM";
        fi.setChecksum(newChecksum);
        assertNotEquals(CHECKSUM, fi.getChecksum());
        assertEquals(newChecksum, fi.getChecksum());

        addStep("Change the date for last file id check", "Should be possible to extract it again.");
        long newLastFileMillis = 1234567L;
        Instant newLastFileCheck = Instant.ofEpochMilli(newLastFileMillis);
        fi.setDateForLastFileIDCheck(newLastFileCheck);
        assertNotEquals(LAST_FILE_CHECK, fi.getDateForLastFileIDCheckInstant());
        assertEquals(newLastFileCheck, fi.getDateForLastFileIDCheckInstant());

        addStep("Change the date for last checksum check", "Should be possible to extract it again.");
        long newLastChecksumMillis = 7654321L;
        Instant newLastChecksumCheck = Instant.ofEpochMilli(newLastChecksumMillis);
        fi.setDateForLastChecksumCheck(newLastChecksumCheck);
        assertNotEquals(LAST_CHECKSUM_CHECK, fi.getDateForLastChecksumCheckInstant());
        assertEquals(newLastChecksumCheck, fi.getDateForLastChecksumCheckInstant());
    }
}
