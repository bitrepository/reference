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
package org.bitrepository.pillar.integration.func.getchecksums;

import org.apache.commons.codec.DecoderException;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.client.exceptions.NegativeResponseException;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.PillarFunctionTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.util.List;

import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertTrue;

public class GetChecksumTest extends PillarFunctionTest {

    @BeforeClass
    public void retrieveFirst2Files() {
        //ToDo
    }

    @Test ( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST} )
    public void md5ChecksumsForAllFilesTest() throws NegativeResponseException {
        addDescription("Test the pillar support for MD5 type checksums");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);

        addStep("Request MD5 checksums for all files on the pillar",
            "A list (at least 2 long) of MD5 checksums should be returned.");
        ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
        checksumSpec.setChecksumType(ChecksumType.MD5);
        List<ChecksumDataForChecksumSpecTYPE> checksums = pillarFileManager.getChecksums(checksumSpec,
                null, null);
        assertTrue(checksums.size() >= 2, "The length of the returned checksums were less that 2");

        addStep("Retrieve the first two files and verify that the checksums are correct",
            "Not implemented");
        // ToDo implement this
    }
    
    @Test ( groups = {PillarTestGroups.FULL_PILLAR_TEST} )
    public void sha1ChecksumsForDefaultTest() throws NegativeResponseException {
        addDescription("Test the pillar support for SHA1 type checksums");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);

        addStep("Request SHA1 checksums for the DefaultFile on the pillar",
            "The SHA1 checksum for the default file should be returned should be returned (Not checked yet).");
        ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
        checksumSpec.setChecksumType(ChecksumType.SHA1);
        List<ChecksumDataForChecksumSpecTYPE> checksums = pillarFileManager.getChecksums(
                checksumSpec, null, DEFAULT_FILE_ID);
        assertNotNull(checksums.get(0));
    }

    @Test ( groups = {PillarTestGroups.FULL_PILLAR_TEST} )
    public void md5SaltChecksumsForDefaultTest() throws NegativeResponseException {
        addDescription("Test the pillar support for MD5 type checksums with a salt");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);

        addStep("Request salted MD5 checksums for the default on the pillar",
            "The correct of SHA1 checksum should be returned (Not checked yet).");
        ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
        checksumSpec.setChecksumType(ChecksumType.HMAC_MD5);
        try {
            checksumSpec.setChecksumSalt(Base16Utils.encodeBase16("abab"));
        } catch (DecoderException e) {
            System.err.println(e.getMessage());
        }
        List<ChecksumDataForChecksumSpecTYPE> checksums = pillarFileManager.getChecksums(
                checksumSpec, null, DEFAULT_FILE_ID);
        assertNotNull(checksums.get(0));
    }
    
    @Test ( groups = {PillarTestGroups.FULL_PILLAR_TEST} )
    public void sha1SaltChecksumsForDefaultTest() throws NegativeResponseException {
        addDescription("Test the pillar support for SHA1 type checksums with a salt");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);

        addStep("Request salted SHA1 checksums for the default on the pillar",
            "The correct of SHA1 checksum should be returned (Not checked yet).");
        ChecksumSpecTYPE checksumSpec = new ChecksumSpecTYPE();
        checksumSpec.setChecksumType(ChecksumType.HMAC_SHA1);
        try {
            checksumSpec.setChecksumSalt(Base16Utils.encodeBase16("abab"));
        } catch (DecoderException e) {
            System.err.println(e.getMessage());
        }
        List<ChecksumDataForChecksumSpecTYPE> checksums = pillarFileManager.getChecksums(
                checksumSpec, null, DEFAULT_FILE_ID);
        assertNotNull(checksums.get(0));
    }    
}
