/*
 * #%L
 * Bitrepository Reference Pillar
 * 
 * $Id: ReplaceFileOnReferencePillarTest.java 589 2011-12-01 15:34:42Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org/bitrepository/pillar/ReplaceFileOnReferencePillarTest.java $
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar.checksumpillar;

import java.util.Date;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileProgressResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.messagefactories.ReplaceFileMessageFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the ReplaceFile functionality on the ReferencePillar.
 */
public class ReplaceFileOnChecksumPillarTest extends ChecksumPillarTest {
    private ReplaceFileMessageFactory msgFactory;
    private static final Long FILE_SIZE = 1L;

    public void initializeCUT() {
        super.initializeCUT();
        msgFactory = new ReplaceFileMessageFactory(settingsForTestClient, getPillarID(), pillarDestinationId);
    }

    // Failing because the replace handler attempts to download the file. It should only download file if no checksum
    // is provided for the new file. See BITMAG-790 - Checksum pillar always tries to download new file for replace
    // file.
    @Test( groups = {"failing", "pillartest"})
    public void pillarReplaceTestSuccessCase() throws Exception {
        addDescription("Tests the replace functionality of the reference pillar for the successful scenario.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        initializeCacheWithMD5ChecksummedFile();

        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForReplaceFileRequest identifyRequest = msgFactory.createIdentifyPillarsForReplaceFileRequest(
                DEFAULT_FILE_ID, FILE_SIZE);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response getPillarID() the pillar.", "The pillar should make a response.");
        IdentifyPillarsForReplaceFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForReplaceFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse.getCorrelationID(), identifyRequest.getCorrelationID());
        Assert.assertEquals(receivedIdentifyResponse.getFileID(), DEFAULT_FILE_ID);
        Assert.assertEquals(receivedIdentifyResponse.getFrom(), getPillarID());
        Assert.assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        Assert.assertEquals(receivedIdentifyResponse.getReplyTo(), pillarDestinationId);
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);
        
        addStep("Create and send the actual Replace message to the pillar.", 
                "Should be received and handled by the pillar.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(
                csData, NON_DEFAULT_CS, csSpec, csSpec, DEFAULT_FILE_ADDRESS, DEFAULT_FILE_ID,
                FILE_SIZE);
        replaceRequest.setCorrelationID(identifyRequest.getCorrelationID());
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the ProgressResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileProgressResponse progressResponse =
                clientReceiver.waitForMessage(ReplaceFileProgressResponse.class);
        Assert.assertEquals(progressResponse.getCorrelationID(), identifyRequest.getCorrelationID());
        Assert.assertEquals(progressResponse.getFileID(), DEFAULT_FILE_ID);
        Assert.assertEquals(progressResponse.getFrom(), getPillarID());
        Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
        Assert.assertEquals(progressResponse.getReplyTo(), pillarDestinationId);
        Assert.assertEquals(progressResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        
        addStep("Retrieve the FinalResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.
                OPERATION_COMPLETED);
        
        Assert.assertNotNull(finalResponse);
        Assert.assertEquals(finalResponse.getCorrelationID(), replaceRequest.getCorrelationID());
        Assert.assertEquals(finalResponse.getFileID(), DEFAULT_FILE_ID);
        Assert.assertEquals(finalResponse.getFrom(), getPillarID());
        Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
        Assert.assertEquals(finalResponse.getReplyTo(), pillarDestinationId);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        
        // validating the checksum
        Assert.assertEquals(finalResponse.getFileID(), DEFAULT_FILE_ID, "The FileID of this test.");
        Assert.assertNotNull(finalResponse.getChecksumDataForNewFile(), "The results should contain a ");
        ChecksumDataForFileTYPE responseChecksumDataForOldFile = finalResponse.getChecksumDataForExistingFile();
        Assert.assertNotNull(responseChecksumDataForOldFile.getChecksumSpec());
        Assert.assertEquals(responseChecksumDataForOldFile.getChecksumSpec(), replaceRequest.getChecksumRequestForNewFile(), 
                "Should return the same type of checksum as requested.");
        Assert.assertEquals(Base16Utils.decodeBase16(responseChecksumDataForOldFile.getChecksumValue()), 
                DEFAULT_MD5_CHECKSUM);

        ChecksumDataForFileTYPE receivedChecksumData = finalResponse.getChecksumDataForNewFile();
        Assert.assertNotNull(receivedChecksumData.getChecksumSpec());
        Assert.assertEquals(receivedChecksumData.getChecksumSpec(), replaceRequest.getChecksumRequestForNewFile(), 
                "Should return the same type of checksum as requested.");
        Assert.assertEquals(receivedChecksumData.getChecksumValue(), TestFileHelper.getDefaultFileChecksum().getChecksumValue());

        addStep("Check the cached checksum.", "Should have been replaced by the new checksum.");
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID),
                Base16Utils.decodeBase16(TestFileHelper.getDefaultFileChecksum().getChecksumValue()));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarReplaceFileTestFailedNoSuchFileDuringIdentify() throws Exception {
        addDescription("Tests the ReplaceFile functionality of the checksum pillar for the scenario when the file does not exist.");
        initializeCacheWithMD5ChecksummedFile();

        addStep("Create and send the identify request message for a non existing file.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForReplaceFileRequest identifyRequest =
                msgFactory.createIdentifyPillarsForReplaceFileRequest("NON-EXISTING-FILE", FILE_SIZE);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response from the checksum pillar.",
                "The checksum pillar should make a response for 'FILE_NOT_FOUND'.");
        IdentifyPillarsForReplaceFileResponse receivedIdentifyResponse =
                clientReceiver.waitForMessage(IdentifyPillarsForReplaceFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.FILE_NOT_FOUND_FAILURE);

        addStep("Validate the content of the cache", "Should not contain the checksum of the file");
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarDeleteFileTestFailedNoSuchFileDuringOperation() throws Exception {
        addDescription("Tests the DeleteFile functionality of the reference pillar for the scenario when the file " +
                "does not exist.");
        initializeCacheWithMD5ChecksummedFile();

        addStep("Send a deleteFileRequest for a none-existing file.",
                "A FILE_NOT_FOUND_FAILURE response should be return, and the original file should remain in the cache.");
        messageBus.sendMessage(msgFactory.createReplaceFileRequest(csData, TestFileHelper.getDefaultFileChecksum(),
                csSpec, csSpec, DEFAULT_FILE_ADDRESS, "NoneExistingFile", FILE_SIZE));

        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarReplaceFileTestMissingExistingChecksumArgument() throws Exception {
        addDescription("Tests that a missing 'ChecksumOnExistingFile' will not delete the file.");
        Assert.assertTrue(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
        initializeCacheWithMD5ChecksummedFile();
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
        messageBus.sendMessage(msgFactory.createReplaceFileRequest(null, TestFileHelper.getDefaultFileChecksum(),
                csSpec, csSpec, DEFAULT_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));
        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE);
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarReplaceFileTestBadExistingChecksumArgument() throws Exception {
        addDescription("Tests that a wrong checksum in 'ChecksumOnExistingFile' will not delete the file.");
        Assert.assertTrue(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
        initializeCacheWithMD5ChecksummedFile();
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
        
        ChecksumDataForFileTYPE badData = new ChecksumDataForFileTYPE();
        badData.setCalculationTimestamp(CalendarUtils.getEpoch());
        badData.setChecksumSpec(csSpec);
        badData.setChecksumValue(Base16Utils.encodeBase16("baabbbaaabba"));

        messageBus.sendMessage(msgFactory.createReplaceFileRequest(badData, TestFileHelper.getDefaultFileChecksum(),
                csSpec, csSpec, DEFAULT_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));
        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE);
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
    }

    // Failing because the replace handler attempts to download the file. It should only download file if no checksum
    // is provided for the new file. See BITMAG-790 - Checksum pillar always tries to download new file for replace
    // file.
    @Test( groups = {"failing", "pillartest"})
    public void checksumPillarReplaceFileTestAllowedMissingExistingChecksum() throws Exception {
        addDescription("Tests that a missing 'ChecksumOnExistingFile' will replace the file, when it has been allowed "
                + "to perform destructive operations in the settings.");
        context.getSettings().getCollectionSettings().getProtocolSettings().setRequireChecksumForDestructiveRequests(false);
        Assert.assertFalse(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
        initializeCacheWithMD5ChecksummedFile();
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
        messageBus.sendMessage(msgFactory.createReplaceFileRequest(null, TestFileHelper.getDefaultFileChecksum(),
                csSpec, csSpec, DEFAULT_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));

        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), Base16Utils.decodeBase16(TestFileHelper.getDefaultFileChecksum().getChecksumValue()));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarReplaceFileTestMissingNewChecksumArgument() throws Exception {
        addDescription("Tests that a missing 'ChecksumOnNewFile' will replace the file, if it is required but not given.");
        context.getSettings().getCollectionSettings().getProtocolSettings().setRequireChecksumForNewFileRequests(true);
        Assert.assertTrue(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForNewFileRequests());
        initializeCacheWithMD5ChecksummedFile();
        messageBus.sendMessage(msgFactory.createReplaceFileRequest(csData, null, csSpec, csSpec, DEFAULT_FILE_ADDRESS, DEFAULT_FILE_ID, 1L));
        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarReplaceFileTestBadNewChecksumArgument() throws Exception {
        addDescription("Tests that a wrong checksum in 'ChecksumOnNewFile' will not delete the file.");
        Assert.assertTrue(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
        initializeCacheWithMD5ChecksummedFile();
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
        
        ChecksumDataForFileTYPE badData = new ChecksumDataForFileTYPE();
        badData.setCalculationTimestamp(CalendarUtils.getEpoch());
        badData.setChecksumSpec(csSpec);
        badData.setChecksumValue(Base16Utils.encodeBase16("baabbbaaabba"));

        messageBus.sendMessage(msgFactory.createReplaceFileRequest(csData, badData, csSpec, csSpec,
                DEFAULT_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));
        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
    }

    // Failing because the replace handler attempts to download the file. It should only download file if no checksum
    // is provided for the new file. See BITMAG-790 - Checksum pillar always tries to download new file for replace
    // file.
    @Test( groups = {"failing", "pillartest"})
    public void checksumPillarReplaceFileTestAllowedMissingNewChecksum() throws Exception {
        addDescription("Tests that a missing 'ChecksumOnNewFile' will replace the file, if it is it not required nor given.");
        context.getSettings().getCollectionSettings().getProtocolSettings().setRequireChecksumForNewFileRequests(false);
        Assert.assertFalse(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForNewFileRequests());
        initializeCacheWithMD5ChecksummedFile();
        messageBus.sendMessage(msgFactory.createReplaceFileRequest(csData, null, csSpec, csSpec, DEFAULT_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));

        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), Base16Utils.decodeBase16(TestFileHelper.getDefaultFileChecksum().getChecksumValue()));
    }
 
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarReplaceFileTestBadExistingChecksumSpec() throws Exception {
        addDescription("Tests that bad checksum spec in 'ChecksumOnExistingFile' will not replace the file.");
        context.getSettings().getCollectionSettings().getProtocolSettings().setRequireChecksumForNewFileRequests(false);
        Assert.assertFalse(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForNewFileRequests());
        initializeCacheWithMD5ChecksummedFile();
        
        ChecksumSpecTYPE badCsType = new ChecksumSpecTYPE();
        badCsType.setChecksumType(ChecksumType.OTHER);
        badCsType.setOtherChecksumType("NOT-EXISTING-TYPE");
        ChecksumDataForFileTYPE badData = new ChecksumDataForFileTYPE();
        badData.setCalculationTimestamp(CalendarUtils.getEpoch());
        badData.setChecksumSpec(badCsType);
        badData.setChecksumValue(Base16Utils.encodeBase16(DEFAULT_MD5_CHECKSUM));

        messageBus.sendMessage(msgFactory.createReplaceFileRequest(badData, TestFileHelper.getDefaultFileChecksum(),
                csSpec, csSpec, DEFAULT_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));

        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.REQUEST_NOT_SUPPORTED);
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarReplaceFileTestBadNewChecksumSpec() throws Exception {
        addDescription("Tests that bad checksum spec in 'ChecksumOnNewFile' will not replace the file.");
        context.getSettings().getCollectionSettings().getProtocolSettings().setRequireChecksumForNewFileRequests(false);
        Assert.assertFalse(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForNewFileRequests());
        initializeCacheWithMD5ChecksummedFile();
        
        ChecksumSpecTYPE badCsType = new ChecksumSpecTYPE();
        badCsType.setChecksumType(ChecksumType.OTHER);
        badCsType.setOtherChecksumType("NOT-EXISTING-TYPE");
        ChecksumDataForFileTYPE badData = new ChecksumDataForFileTYPE();
        badData.setCalculationTimestamp(CalendarUtils.getEpoch());
        badData.setChecksumSpec(badCsType);
        badData.setChecksumValue(Base16Utils.encodeBase16(DEFAULT_MD5_CHECKSUM));

        messageBus.sendMessage(msgFactory.createReplaceFileRequest(csData, badData, csSpec, csSpec, DEFAULT_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));

        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.REQUEST_NOT_SUPPORTED);
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarReplaceFileTestBadExistingChecksumRequestSpec() throws Exception {
        addDescription("Tests that bad checksum spec in 'ChecksumSpecForExistingFile' will not replace the file.");
        context.getSettings().getCollectionSettings().getProtocolSettings().setRequireChecksumForNewFileRequests(false);
        Assert.assertFalse(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForNewFileRequests());
        initializeCacheWithMD5ChecksummedFile();
        
        ChecksumSpecTYPE badCsType = new ChecksumSpecTYPE();
        badCsType.setChecksumType(ChecksumType.OTHER);
        badCsType.setOtherChecksumType("NOT-EXISTING-TYPE");

        messageBus.sendMessage(msgFactory.createReplaceFileRequest(csData, TestFileHelper.getDefaultFileChecksum(),
                badCsType, csSpec, DEFAULT_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));

        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.REQUEST_NOT_SUPPORTED);
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarReplaceFileTestBadNewChecksumRequestSpec() throws Exception {
        addDescription("Tests that bad checksum spec in 'ChecksumSpecForNewFile' will not replace the file.");
        context.getSettings().getCollectionSettings().getProtocolSettings().setRequireChecksumForNewFileRequests(false);
        Assert.assertFalse(context.getSettings().getCollectionSettings().getProtocolSettings().isRequireChecksumForNewFileRequests());
        initializeCacheWithMD5ChecksummedFile();
        
        ChecksumSpecTYPE badCsType = new ChecksumSpecTYPE();
        badCsType.setChecksumType(ChecksumType.OTHER);
        badCsType.setOtherChecksumType("NOT-EXISTING-TYPE");

        messageBus.sendMessage(msgFactory.createReplaceFileRequest(csData, TestFileHelper.getDefaultFileChecksum(),
                csSpec, badCsType, DEFAULT_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));

        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.REQUEST_NOT_SUPPORTED);
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
    }

    // Failing because the replace handler attempts to download the file. It should only download file if no checksum
    // is provided for the new file. See BITMAG-790 - Checksum pillar always tries to download new file for replace
    // file.
    @Test( groups = {"failing", "pillartest"})
    public void checksumPillarReplaceFileSuccessWithoutChecksums() throws Exception {
        addDescription("Tests that it is possible to replace a file without any checksums if settings allows it.");
        context.getSettings().getCollectionSettings().getProtocolSettings().setRequireChecksumForNewFileRequests(false);
        context.getSettings().getCollectionSettings().getProtocolSettings().setRequireChecksumForDestructiveRequests(false);
        initializeCacheWithMD5ChecksummedFile();
        messageBus.sendMessage(msgFactory.createReplaceFileRequest(
                null, null, null, null, DEFAULT_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));

        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarReplaceFileBadURL() throws Exception {
        addDescription("Tests the handling of a bad URL in the request.");
        initializeCacheWithMD5ChecksummedFile();
        String fileAddress = "http://127.0.0.1/¾" + new Date().getTime();
        messageBus.sendMessage(msgFactory.createReplaceFileRequest(csData, TestFileHelper.getDefaultFileChecksum(), csSpec, csSpec, fileAddress, DEFAULT_FILE_ID, FILE_SIZE));

        ReplaceFileFinalResponse finalResponse = clientReceiver.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.FILE_TRANSFER_FAILURE);
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
    }
}
