/*
 * #%L
 * Bitrepository Reference Pillar
 * %%
 * Copyright (C) 2010 - 2015 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar.store;

import org.bitrepository.SuiteInfoParameterResolver;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.store.checksumcache.MemoryCacheMock;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumStore;
import org.bitrepository.pillar.store.filearchive.CollectionArchiveManager;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.exception.RequestHandlerException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import static org.bitrepository.protocol.utils.AllureTestUtils.addDescription;
import static org.bitrepository.protocol.utils.AllureTestUtils.addStep;

@ExtendWith(SuiteInfoParameterResolver.class)
public class FullPillarModelTest extends DefaultFixturePillarTest {
    FileStorageModel pillarModel;
    ChecksumStore cache;
    FileStore archives;
    protected AlarmDispatcher alarmDispatcher;
    ChecksumSpecTYPE defaultCsType;
    ChecksumSpecTYPE nonDefaultCsType;

    protected static final String EMPTY_HMAC_SHA385_CHECKSUM =
            "3e7012b39d4f6c503b2a4846fff3f4d0d61fb1a58b81035765f283cfa5f1b93e57ded9e0a946447ff24e5c9be39c8573";
    protected static final String EMPTY_MD5_CHECKSUM = "d41d8cd98f00b204e9800998ecf8427e";

    @Override
    protected void initializeCUT() {
        cache = new MemoryCacheMock();
        archives = new CollectionArchiveManager(settingsForCUT);
        alarmDispatcher = new AlarmDispatcher(settingsForCUT, messageBus);
        pillarModel = new FileStorageModel(archives, cache, alarmDispatcher, settingsForCUT);

        defaultCsType = ChecksumUtils.getDefault(settingsForCUT);

        nonDefaultCsType = new ChecksumSpecTYPE();
        nonDefaultCsType.setChecksumType(ChecksumType.HMAC_SHA384);
        nonDefaultCsType.setChecksumSalt(new byte[]{'a', 'z'});
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void testPillarModelBasicFunctionality() throws Exception {
        addDescription("Test the basic functions of the full reference pillar model.");
        addStep("Check the pillar id in the pillar model", "Identical to the one from the test.");
        Assertions.assertEquals(getPillarID(), pillarModel.getPillarID());

        addStep("Ask whether it can handle a file of size 0",
                "Should not throw an exception");
        pillarModel.verifyEnoughFreeSpaceLeftForFile(0L, collectionID);

        addStep("Ask whether it can handle a file of maximum size",
                "Should throw an exception");
        try {
            pillarModel.verifyEnoughFreeSpaceLeftForFile(Long.MAX_VALUE, collectionID);
            Assertions.fail("Should not be possible to verify such amount of space left.");
        } catch (RequestHandlerException e) {
            // expected.
        }

        addStep("Check the ChecksumPillarSpec",
                "Must be null, since it is full reference pillar and not a checksums pillar");
        Assertions.assertNull(pillarModel.getChecksumPillarSpec());
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void testPillarModelHasFile() throws Exception {
        addDescription("Test that the file exists, when placed in the archive and cache");
        addStep("Setup", "Should place the 'existing file' in the directory.");
        initializeWithDefaultFile();

        addStep("Check whether file exists and retrieve it.", "Should be the empty file.");
        Assertions.assertTrue(pillarModel.hasFileID(defaultFileId, collectionID));
        FileInfo fileInfo = pillarModel.getFileInfoForActualFile(defaultFileId, collectionID);
        Assertions.assertEquals(0L, fileInfo.getSize());
        Assertions.assertEquals(defaultFileId, fileInfo.getFileID());

        addStep("Verify that no exceptions are thrown when verifying file existance.", "Should exist.");
        pillarModel.verifyFileExists(defaultFileId, collectionID);

        addStep("Check retrieval of non-default checksum", "");
        String md5Checksum = pillarModel.getNonDefaultChecksum(defaultFileId, collectionID, defaultCsType);
        Assertions.assertEquals(EMPTY_MD5_CHECKSUM, md5Checksum);
        String otherChecksum = pillarModel.getNonDefaultChecksum(defaultFileId, collectionID, nonDefaultCsType);
        Assertions.assertEquals(EMPTY_HMAC_SHA385_CHECKSUM, otherChecksum);
    }

    @Test
    @Tag("regressiontest")
    @Tag("pillartest")
    public void testPillarModelNoFile() throws Exception {
        addDescription("Test that the file exists, when placed in the archive and cache");
        addStep("Setup", "Should place the 'existing file' in the directory.");
        emptyArchive();

        addStep("Check whether file exists and try to retrieve it.",
                "Should say no, and throw exception when attempted to be retrieved.");
        Assertions.assertFalse(pillarModel.hasFileID(defaultFileId, collectionID));
        try {
            pillarModel.getFileInfoForActualFile(defaultFileId, collectionID);
            Assertions.fail("Must throw an exception, when asked for a file it does not have.");
        } catch (Exception e) {
            // expected
        }

        addStep("Verify that anexceptions are thrown when verifying file existance.",
                "Should not exist.");
        try {
            pillarModel.verifyFileExists(defaultFileId, collectionID);
            Assertions.fail("Must throw an exception here!");
        } catch (Exception e) {
            // expected
        }
    }

    private void emptyArchive() {
        if (archives.hasFile(defaultFileId, collectionID)) {
            archives.deleteFile(defaultFileId, collectionID);
        }
        archives.ensureFileNotInTmpDir(defaultFileId, collectionID);
    }

    private void initializeWithDefaultFile() throws IOException {
        emptyArchive();

        archives.downloadFileForValidation(defaultFileId, collectionID, new ByteArrayInputStream(new byte[0]));
        archives.moveToArchive(defaultFileId, collectionID);
        pillarModel.recalculateChecksum(defaultFileId, collectionID);
    }
}
