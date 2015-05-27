package org.bitrepository.pillar.store;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.store.checksumcache.MemoryCacheMock;
import org.bitrepository.pillar.store.checksumdatabase.ChecksumStore;
import org.bitrepository.pillar.store.filearchive.CollectionArchiveManager;
import org.bitrepository.protocol.LocalFileExchange;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.exception.RequestHandlerException;
import org.testng.annotations.Test;

public class FullPillarModelTest extends DefaultFixturePillarTest {
    FileStorageModel pillarModel;
    ChecksumStore cache;
    FileStore archives;
    protected AlarmDispatcher alarmDispatcher;
    ChecksumSpecTYPE defaultCsType;
    ChecksumSpecTYPE nonDefaultCsType;
    
    protected static final String EMPTY_HMAC_SHA385_CHECKSUM = "3e7012b39d4f6c503b2a4846fff3f4d0d61fb1a58b81035765f283cfa5f1b93e57ded9e0a946447ff24e5c9be39c8573";
    protected static final String EMPTY_MD5_CHECKSUM = "d41d8cd98f00b204e9800998ecf8427e";

    @Override
    protected void initializeCUT() {
        cache = new MemoryCacheMock();
        archives = new CollectionArchiveManager(settingsForCUT);
        alarmDispatcher = new AlarmDispatcher(settingsForCUT, messageBus);
        pillarModel = new FileStorageModel(archives, cache, alarmDispatcher, settingsForCUT,
                new LocalFileExchange("src/test/resources"));
        
        defaultCsType = ChecksumUtils.getDefault(settingsForCUT);
        
        nonDefaultCsType = new ChecksumSpecTYPE();
        nonDefaultCsType.setChecksumType(ChecksumType.HMAC_SHA384);
        nonDefaultCsType.setChecksumSalt(new byte[]{'a', 'z'});
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testPillarModelBasicFunctionality() throws Exception {
        addDescription("Test the basic functions of the full reference pillar model.");
        addStep("Check the pillar id in the pillar model", "Identical to the one from the test.");
        assertEquals(pillarModel.getPillarID(), getPillarID());
        
        addStep("Ask whether it can handle a file of size 0", 
                "Should not throw an exception");
        pillarModel.verifyEnoughFreeSpaceLeftForFile(0L, collectionID);
        
        addStep("Ask whether it can handle a file of maximum size",
                "Should throw an exception");
        try {
            pillarModel.verifyEnoughFreeSpaceLeftForFile(Long.MAX_VALUE, collectionID);
            fail("Should not be possible to verify such amount of space left.");
        } catch (RequestHandlerException e) {
            // expected.
        }
        
        addStep("Check the ChecksumPillarSpec", 
                "Must be null, since it is full reference pillar and not a checksums pillar");
        assertNull(pillarModel.getChecksumPillarSpec());
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testPillarModelHasFile() throws Exception {
        addDescription("Test that the file exists, when placed in the archive and cache");
        addStep("Setup", "Should place the 'existing file' in the directory.");
        initializeWithDefaultFile();
        
        addStep("Check whether file exists and retrieve it.", "Should be the empty file.");
        assertTrue(pillarModel.hasFileID(DEFAULT_FILE_ID, collectionID));
        FileInfo fileInfo = pillarModel.getFileInfoForActualFile(DEFAULT_FILE_ID, collectionID);
        assertEquals(fileInfo.getSize(), 0L);
        assertEquals(fileInfo.getFileID(), DEFAULT_FILE_ID);
        
        addStep("Verify that no exceptions are thrown when verifying file existance.", "Should exist.");
        pillarModel.verifyFileExists(DEFAULT_FILE_ID, collectionID);
        
        addStep("Check retrieval of non-default checksum", "");
        String md5Checksum = pillarModel.getNonDefaultChecksum(DEFAULT_FILE_ID, collectionID, defaultCsType);
        assertEquals(EMPTY_MD5_CHECKSUM, md5Checksum);        
        String otherChecksum = pillarModel.getNonDefaultChecksum(DEFAULT_FILE_ID, collectionID, nonDefaultCsType);
        assertEquals(EMPTY_HMAC_SHA385_CHECKSUM, otherChecksum);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testPillarModelNoFile() throws Exception {
        addDescription("Test that the file exists, when placed in the archive and cache");
        addStep("Setup", "Should place the 'existing file' in the directory.");
        emptyArchive();
        
        addStep("Check whether file exists and try to retrieve it.", 
                "Should say no, and throw exception when attempted to be retrieved.");
        assertFalse(pillarModel.hasFileID(DEFAULT_FILE_ID, collectionID));
        try {
            pillarModel.getFileInfoForActualFile(DEFAULT_FILE_ID, collectionID);
            fail("Must throw an exception, when asked for a file it does not have.");
        } catch (Exception e) {
            // expected
        }
        
        addStep("Verify that anexceptions are thrown when verifying file existance.", "Should not exist.");
        try {
            pillarModel.verifyFileExists(DEFAULT_FILE_ID, collectionID);
            fail("Must throw an exception here!");
        } catch (Exception e) {
            // expected
        }
        
        
//        pillarModel.
    }
    
    private void emptyArchive() throws IOException {
        if(archives.hasFile(DEFAULT_FILE_ID, collectionID)) {
            archives.deleteFile(DEFAULT_FILE_ID, collectionID);
        }
        archives.ensureFileNotInTmpDir(DEFAULT_FILE_ID, collectionID);
    }
    
    private void initializeWithDefaultFile() throws IOException {
        emptyArchive();
        
        archives.downloadFileForValidation(DEFAULT_FILE_ID, collectionID, new ByteArrayInputStream(new byte[0]));
        archives.moveToArchive(DEFAULT_FILE_ID, collectionID);
        pillarModel.verifyFileToCacheConsistency(DEFAULT_FILE_ID, collectionID);
    }
}
