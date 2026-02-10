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

import org.bitrepository.common.utils.CalendarUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.XMLGregorianCalendar;

import static javax.xml.datatype.DatatypeConstants.EQUAL;
import static org.bitrepository.common.utils.CalendarUtils.getFromMillis;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class FileInfoTest extends ExtendedTestCase {

    private static final String FILE_ID = "TEST-FILE";
    private static final long LAST_FILE_CHECK_MILLIS = 1000000;
    private static final XMLGregorianCalendar LAST_FILE_CHECK = CalendarUtils.getFromMillis(LAST_FILE_CHECK_MILLIS);
    private static final String CHECKSUM = "CHECKSUM";
    private static final long LAST_CHECKSUM_CHECK_MILLIS = 2000000;
    private static final XMLGregorianCalendar LAST_CHECKSUM_CHECK = CalendarUtils.getFromMillis(LAST_CHECKSUM_CHECK_MILLIS);
    private static final String PILLAR_ID = "test-pillar";
    private static final Long FILE_SIZE = 12345L;

    @Test
    @Tag("regressiontest")
    @Tag("integritytest")
    public void testFileInfo() {
        addDescription("Tests the FileInfo element. Adds all data and extracts it again.");
        addStep("Setup the file info.", "Should be possible to extract all the data again.");
        FileInfo fi = new FileInfo(FILE_ID, LAST_FILE_CHECK, CHECKSUM, FILE_SIZE, LAST_CHECKSUM_CHECK, PILLAR_ID);

        assertEquals(FILE_ID, fi.getFileId());
        assertEquals(LAST_FILE_CHECK_MILLIS, fi.getDateForLastFileIDCheck().toGregorianCalendar().getTimeInMillis());
        assertEquals(CHECKSUM, fi.getChecksum());
        assertEquals(LAST_CHECKSUM_CHECK_MILLIS, fi.getDateForLastChecksumCheck().toGregorianCalendar().getTimeInMillis());
        assertEquals(PILLAR_ID, fi.getPillarId());
        assertEquals(FILE_SIZE, fi.getFileSize());

        addStep("Change the checksum", "Should be possible to extract it again.");
        String newChecksum = "NEW-CHECKSUM";
        fi.setChecksum(newChecksum);
        assertNotEquals(CHECKSUM, newChecksum);
        assertEquals(newChecksum, fi.getChecksum());

        addStep("Change the date for last file id check", "Should be possible to extract it again.");
        long newLastFileMillis = 1234567;
        XMLGregorianCalendar newLastFileCheck = getFromMillis(newLastFileMillis);
        fi.setDateForLastFileIDCheck(newLastFileCheck);
        assertNotEquals(EQUAL, LAST_FILE_CHECK.compare(newLastFileCheck));
        assertEquals(newLastFileMillis, fi.getDateForLastFileIDCheck().toGregorianCalendar().getTimeInMillis());

        addStep("Change the date for last checksum check", "Should be possible to extract it again.");
        long newLastChecksumMillis = 7654321;
        XMLGregorianCalendar newLastChecksumCheck = getFromMillis(newLastChecksumMillis);
        fi.setDateForLastChecksumCheck(newLastChecksumCheck);
        assertNotEquals(EQUAL, LAST_CHECKSUM_CHECK.compare(newLastChecksumCheck));
        assertEquals(newLastChecksumMillis, fi.getDateForLastChecksumCheck().toGregorianCalendar().getTimeInMillis());
    }
}
