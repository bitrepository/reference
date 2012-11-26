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
package org.bitrepository.common.utils;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

public class TestFileHelper {
    public static final String DEFAULT_FILE_ID = ClientTestMessageFactory.FILE_ID_DEFAULT;
    public static final String TEST_RESOURCES_PATH = "test-files/";

    private TestFileHelper() {}

    public static InputStream getDefaultFile() {
        return getFile(DEFAULT_FILE_ID);
    }

    public static ChecksumDataForFileTYPE getDefaultFileChecksum() {
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        checksumData.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
        ChecksumSpecTYPE checksumSpecTYPE = new ChecksumSpecTYPE();
        checksumSpecTYPE.setChecksumType(ChecksumType.MD5);
        checksumData.setChecksumSpec(checksumSpecTYPE);
        checksumData.setChecksumValue(Base16Utils.encodeBase16(
                ChecksumUtils.generateChecksum(getDefaultFile(), checksumSpecTYPE)));
        return checksumData;
    }

    public static long getFileSize(File file) {
        return file.length();
    }

    public static InputStream getFile(String name) {
        String fullName = TEST_RESOURCES_PATH+name;
        InputStream fileStream = TestFileHelper.class.getClassLoader().getResourceAsStream(fullName);
        assert (fileStream != null) : "Unable to find " + fullName + " in classpath";
        return fileStream;
    }

    public static String createUniquePrefix(String testName) {
        return testName + "-" + System.getProperty("user.name") + "-" + "-Test-File-" + System.currentTimeMillis();
    }

    public static String[] createFileIDs(int numberToCreate, String testName) {
        String uniquePrefix = createUniquePrefix(testName);
        String[] fileIDs = new String[numberToCreate];
        for (int i = 0 ; i<numberToCreate ; i++) {
            fileIDs[i] = uniquePrefix + "-" + (i+1) +".txt";
        }
        return fileIDs;
    }
}
