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
package org.bitrepository.pillar.integration;

import java.io.File;
import java.util.Date;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

public class TestFileHelper {
    public static final String DEFAULT_FILE_ID = ClientTestMessageFactory.FILE_ID_DEFAULT;
    public static final String TEST_RESOURCES_PATH = "src/test/resources/";
    private static ChecksumDataForFileTYPE DEFAULT_FILE_CHECKSUM;

    private TestFileHelper() {}

    public static File getDefaultFile() {
        return getFile(DEFAULT_FILE_ID);
    }

    public static ChecksumDataForFileTYPE getDefaultFileChecksum() {
        if(DEFAULT_FILE_CHECKSUM == null) {
            DEFAULT_FILE_CHECKSUM = new ChecksumDataForFileTYPE();
            DEFAULT_FILE_CHECKSUM.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
            ChecksumSpecTYPE checksumSpecTYPE = new ChecksumSpecTYPE();
            checksumSpecTYPE.setChecksumType(ChecksumType.MD5);
            DEFAULT_FILE_CHECKSUM.setChecksumSpec(checksumSpecTYPE);
            DEFAULT_FILE_CHECKSUM.setChecksumValue(Base16Utils.encodeBase16(
                    ChecksumUtils.generateChecksum(getDefaultFile(), ChecksumType.MD5)));
        }
        return DEFAULT_FILE_CHECKSUM;
    }

    public static String getFileName(File file) {
        return DEFAULT_FILE_ID + new Date().getTime();
    }

    public static long getFileSize(File file) {
        return file.length();
    }

    public static File getFile(String name) {
        File file = new File(TEST_RESOURCES_PATH, name);
        assert(file.isFile());
        return file;
    }


    public static String[] createFileIDs(int numberToCreate, String prefix) {
        String uniquePrefix = prefix + "-" + System.getProperty("user.name") + "-" +
                System.currentTimeMillis();
        String[] fileIDs = new String[numberToCreate];
        for (int i = 0 ; i<numberToCreate ; i++) {
            fileIDs[i] = uniquePrefix + "-" + (i+1) +".txt";
        }
        return fileIDs;
    }

}
