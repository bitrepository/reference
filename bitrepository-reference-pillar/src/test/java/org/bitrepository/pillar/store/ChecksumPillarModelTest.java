package org.bitrepository.pillar.store;

import static org.testng.Assert.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.store.checksumcache.MemoryCacheMock;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedChecksumResultSet;
import org.bitrepository.pillar.store.filearchive.CollectionArchiveManager;
import org.bitrepository.service.AlarmDispatcher;
import org.bitrepository.service.exception.RequestHandlerException;
import org.bitrepository.settings.referencesettings.ChecksumPillarFileDownload;
import org.testng.annotations.Test;

public class ChecksumPillarModelTest extends DefaultFixturePillarTest {
    ChecksumPillarModel pillarModel;
    ChecksumStore cache;
    protected AlarmDispatcher alarmDispatcher;
    ChecksumSpecTYPE defaultCsType;
    ChecksumSpecTYPE nonDefaultCsType;
    
    protected static final String EMPTY_MD5_CHECKSUM = "d41d8cd98f00b204e9800998ecf8427e";

    @Override
    protected void initializeCUT() {
        cache = new MemoryCacheMock();
        alarmDispatcher = new AlarmDispatcher(settingsForCUT, messageBus);
        pillarModel = new ChecksumPillarModel(cache, alarmDispatcher, settingsForCUT);
        
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
                "Should not be a problem for the checksum pillar.");
        pillarModel.verifyEnoughFreeSpaceLeftForFile(Long.MAX_VALUE, collectionID);
        
        addStep("Check the ChecksumPillarSpec", 
                "Must be the default checksum spec from settings");
        assertEquals(pillarModel.getChecksumPillarSpec(), defaultCsType);
        
        addStep("Checkum whether the checksum pillar should download", 
                "It should say as it is in settings, or return default");
        settingsForCUT.getReferenceSettings().getPillarSettings().setChecksumPillarFileDownload(
                ChecksumPillarFileDownload.ALWAYS_DOWNLOAD);
        assertEquals(pillarModel.getChecksumPillarFileDownload(), 
                ChecksumPillarFileDownload.ALWAYS_DOWNLOAD);
        settingsForCUT.getReferenceSettings().getPillarSettings().setChecksumPillarFileDownload(
                ChecksumPillarFileDownload.NEVER_DOWNLOAD);
        assertEquals(pillarModel.getChecksumPillarFileDownload(), 
                ChecksumPillarFileDownload.NEVER_DOWNLOAD);
        settingsForCUT.getReferenceSettings().getPillarSettings().setChecksumPillarFileDownload(null);
        assertEquals(pillarModel.getChecksumPillarFileDownload(), 
                ChecksumPillarFileDownload.DOWNLOAD_WHEN_MISSING_FROM_MESSAGE);

    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testPillarModelHasFile() throws Exception {
        addDescription("Test that the file exists, when placed in the archive and cache");
        addStep("Setup", "Should place the 'existing file' in the directory.");
        initializeWithDefaultFile();
        
        addStep("Check whether file exists and retrieve it.", 
                "Should have the file ID, but throw an exception when asked for the actual file.");
        assertTrue(pillarModel.hasFileID(DEFAULT_FILE_ID, collectionID));
        try {
            pillarModel.getFileInfoForActualFile(DEFAULT_FILE_ID, collectionID);
            fail("Must throw an exception here!");
        } catch (Exception e) {
            // expected
        }
        
        addStep("Check whether file exists.", "Should not exist.");
        try {
            pillarModel.verifyFileExists(DEFAULT_FILE_ID, collectionID);
            fail("Must throw an exception here");
        } catch (Exception e) {
            // expected
        }

        addStep("Ask for the checksum data for the file with different checksum specs",
                "Should fail, unless asked for the default checksum spec.");
        assertNotNull(pillarModel.getChecksumDataForFile(DEFAULT_FILE_ID, collectionID, defaultCsType));
        try {
            pillarModel.getChecksumDataForFile(DEFAULT_FILE_ID, collectionID, nonDefaultCsType);
            fail("Must throw an exception here");
        } catch (Exception e) {
            // expected
        }

        addStep("Ask for the checksum entry for the file with different checksum specs",
                "Should fail, unless asked for the default checksum spec.");
        assertNotNull(pillarModel.getChecksumEntryForFile(DEFAULT_FILE_ID, collectionID, defaultCsType));
        try {
            pillarModel.getChecksumEntryForFile(DEFAULT_FILE_ID, collectionID, nonDefaultCsType);
            fail("Must throw an exception here");
        } catch (Exception e) {
            // expected
        }
        
        addStep("Ask for the checksum for the file with different checksum specs",
                "Should fail, unless asked for the default checksum spec.");
        assertNotNull(pillarModel.getChecksumForFile(DEFAULT_FILE_ID, collectionID, defaultCsType));
        try {
            pillarModel.getChecksumForFile(DEFAULT_FILE_ID, collectionID, nonDefaultCsType);
            fail("Must throw an exception here");
        } catch (Exception e) {
            // expected
        }
        
        addStep("Check extraction of checksum result set", 
                "Should deliver non-null object when called with default checksum spec, otherwise throw exception.");
        assertNotNull(pillarModel.getChecksumResultSet(null, null, null, collectionID, defaultCsType));
        try {
            pillarModel.getChecksumResultSet(null, null, null, collectionID, nonDefaultCsType);
            fail("Must throw an exception here.");
        } catch (Exception e) {
            // exptected
        }
        
        addStep("Check retrieval of non-default checksum", "");
        try {
            pillarModel.getNonDefaultChecksum(DEFAULT_FILE_ID, collectionID, defaultCsType);
            fail("Must throw an exception here");
        } catch (Exception e) {
            // expected
        }

        addStep("Check retrieval of non-default checksum result set", "");
        try {
            pillarModel.getNonDefaultChecksumResultSet(null, collectionID, defaultCsType);
            fail("Must throw an exception here");
        } catch (Exception e) {
            // expected
        }

        addStep("Test retrieval of single checksum result set",
                "Should return non-null object, unless asked for non-default checksum spec which must raise exception.");
        pillarModel.getSingleChecksumResultSet(DEFAULT_FILE_ID, collectionID, null, null, defaultCsType);
        try {
            pillarModel.getSingleChecksumResultSet(DEFAULT_FILE_ID, collectionID, null, null, nonDefaultCsType);
            fail("Must throw an exception here");
        } catch (Exception e) {
            // expected
        }
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void testPillarModelNoFile() throws Exception {
        addDescription("Test that the file exists, when placed in the archive and cache");
        addStep("Setup", "Should place the 'existing file' in the directory.");
        
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
    }
    
    private void initializeWithDefaultFile() throws IOException {
        cache.insertChecksumCalculation(DEFAULT_FILE_ID, collectionID, EMPTY_MD5_CHECKSUM, new Date());
    }
}
