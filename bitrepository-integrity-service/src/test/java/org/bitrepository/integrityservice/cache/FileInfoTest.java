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

import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.CalendarUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class FileInfoTest extends ExtendedTestCase {
    
    private static final String FILE_ID = "TEST-FILE"; 
    private static final long LAST_FILE_CHECK_MILLIS = 1000000;
    private static final XMLGregorianCalendar LAST_FILE_CHECK = CalendarUtils.getFromMillis(LAST_FILE_CHECK_MILLIS);
    private static final String CHECKSUM = "CHECKSUM";
    private static final ChecksumSpecTYPE CHECKSUM_TYPE = new ChecksumSpecTYPE();
    private static final long LAST_CHECKSUM_CHECK_MILLIS = 2000000;
    private static final XMLGregorianCalendar LAST_CHECKSUM_CHECK = CalendarUtils.getFromMillis(LAST_CHECKSUM_CHECK_MILLIS);
    private static final String PILLAR_ID = "test-pillar";
    
    @Test(groups = {"regressiontest"})
    public void testFileInfo() {
        addDescription("Tests the FileInfo element. Adds all data and extracts it again.");
        addStep("Setup the file info.", "Should be possible to extract all the data again.");
        FileInfo fi = new FileInfo(FILE_ID, LAST_FILE_CHECK, CHECKSUM, CHECKSUM_TYPE, LAST_CHECKSUM_CHECK, PILLAR_ID);
        
        Assert.assertEquals(fi.getFileId(), FILE_ID);
        Assert.assertEquals(fi.getDateForLastFileIDCheck().toGregorianCalendar().getTimeInMillis(), LAST_FILE_CHECK_MILLIS);
        Assert.assertEquals(fi.getChecksum(), CHECKSUM);
        Assert.assertEquals(fi.getChecksumType(), CHECKSUM_TYPE);
        Assert.assertEquals(fi.getDateForLastChecksumCheck().toGregorianCalendar().getTimeInMillis(), LAST_CHECKSUM_CHECK_MILLIS);
        Assert.assertEquals(fi.getPillarId(), PILLAR_ID);
        
        addStep("Change the checksum", "Should be possible to extract it again.");
        String newChecksum = "NEW-CHECKSUM";
        fi.setChecksum(newChecksum);
        Assert.assertFalse(CHECKSUM.equals(newChecksum));
        Assert.assertEquals(fi.getChecksum(), newChecksum);

        addStep("Change the checksum spec type", "Should be possible to extract it again.");
        ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
        csType.setChecksumType(ChecksumType.OTHER);
        fi.setChecksumType(csType);
        Assert.assertFalse(csType == CHECKSUM_TYPE);
        Assert.assertEquals(fi.getChecksumType(), csType);
        
        addStep("Change the date for last file id check", "Should be possible to extract it again.");
        long newLastFileMillis = 1234567;
        XMLGregorianCalendar newLastFileCheck = CalendarUtils.getFromMillis(newLastFileMillis);
        fi.setDateForLastFileIDCheck(newLastFileCheck);
        Assert.assertFalse(LAST_FILE_CHECK.compare(newLastFileCheck) == DatatypeConstants.EQUAL);
        Assert.assertEquals(fi.getDateForLastFileIDCheck().toGregorianCalendar().getTimeInMillis(), newLastFileMillis);
        
        addStep("Change the date for last checksum check", "Should be possible to extract it again.");
        long newLastChecksumMillis = 7654321;
        XMLGregorianCalendar newLastChecksumCheck = CalendarUtils.getFromMillis(newLastChecksumMillis);
        fi.setDateForLastChecksumCheck(newLastChecksumCheck);
        Assert.assertFalse(LAST_CHECKSUM_CHECK.compare(newLastChecksumCheck) == DatatypeConstants.EQUAL);
        Assert.assertEquals(fi.getDateForLastChecksumCheck().toGregorianCalendar().getTimeInMillis(), newLastChecksumMillis);
    }
}
