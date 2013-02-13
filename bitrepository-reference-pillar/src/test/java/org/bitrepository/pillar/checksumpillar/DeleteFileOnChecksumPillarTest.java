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
package org.bitrepository.pillar.checksumpillar;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.pillar.messagefactories.DeleteFileMessageFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

public class DeleteFileOnChecksumPillarTest extends ChecksumPillarTest {
    private DeleteFileMessageFactory msgFactory;

    @Override
    public void initializeCUT() {
        super.initializeCUT();
        msgFactory = new DeleteFileMessageFactory(settingsForTestClient, getPillarID(), pillarDestinationId);
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void generalDeleteFileTest() {
        addDescription("Testing the general delete operation functionality for the checksum pillar.");

        initializeCacheWithMD5ChecksummedFile();

        addStep("Send message for identification of the pillar.", 
                "The checksum pillar receive and handle the message.");
        IdentifyPillarsForDeleteFileRequest identifyRequest =
                msgFactory.createIdentifyPillarsForDeleteFileRequest(DEFAULT_FILE_ID);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Receive and validate response from the checksum pillar.",
                "The pillar should make a positive response.");
        IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse =
                clientReceiver.waitForMessage(IdentifyPillarsForDeleteFileResponse.class);
        Assert.assertNotNull(receivedIdentifyResponse);
        Assert.assertEquals(receivedIdentifyResponse.getCorrelationID(), identifyRequest.getCorrelationID());
        Assert.assertEquals(receivedIdentifyResponse.getFileID(), DEFAULT_FILE_ID);
        Assert.assertEquals(receivedIdentifyResponse.getFrom(), getPillarID());
        Assert.assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        Assert.assertEquals(receivedIdentifyResponse.getReplyTo(), pillarDestinationId);
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);
        
        addStep("Create and send the actual DeleteFile message to the checksum pillar.", 
                "Should be received and handled by the checksum pillar.");
        DeleteFileRequest deleteFileRequest = msgFactory.createDeleteFileRequest(csData, csSpec, DEFAULT_FILE_ID);
        messageBus.sendMessage(deleteFileRequest);
        
        addStep("Retrieve the ProgressResponse for the DeleteFile request", 
                "The DeleteFile progress response should be sent by the checksum pillar.");
        DeleteFileProgressResponse progressResponse = clientReceiver.waitForMessage(DeleteFileProgressResponse.class);
        Assert.assertNotNull(progressResponse);
        Assert.assertEquals(progressResponse.getCorrelationID(), deleteFileRequest.getCorrelationID());
        Assert.assertEquals(progressResponse.getFileID(), DEFAULT_FILE_ID);
        Assert.assertEquals(progressResponse.getFrom(), getPillarID());
        Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
        Assert.assertEquals(progressResponse.getReplyTo(), pillarDestinationId);
        Assert.assertEquals(progressResponse.getResponseInfo().getResponseCode(),
                ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        
        addStep("Retrieve the FinalResponse for the DeleteFile request", 
                "The DeleteFile response should be sent by the checksum pillar.");
        DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);

        Assert.assertNotNull(finalResponse);
        Assert.assertEquals(finalResponse.getCorrelationID(), deleteFileRequest.getCorrelationID());
        Assert.assertEquals(finalResponse.getFileID(), DEFAULT_FILE_ID);
        Assert.assertEquals(finalResponse.getFrom(), getPillarID());
        Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
        Assert.assertEquals(finalResponse.getReplyTo(), pillarDestinationId);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(),
                ResponseCode.OPERATION_COMPLETED);
        //ToDO Implement the assertion below
        //Assert.assertEquals(finalResponse.getChecksumDataForExistingFile(), ??);

        addStep("Validate the content of the cache", "Should no longer contain the checksum of the file");
        Assert.assertNull(cache.getChecksum(DEFAULT_FILE_ID));
    }

    // ToDo add test for AuditTrail information.
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarDeleteFileTestFailedNoSuchFileDuringIdentify() {
        addDescription("Tests the DeleteFile functionality of the reference pillar for the scenario when the file does not exist.");

        addStep("Create and send the identify request message for a non existing file.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForDeleteFileRequest identifyRequest =
                msgFactory.createIdentifyPillarsForDeleteFileRequest("NoneExistingFile");
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response from the checksum pillar.",
                "The checksum pillar should make a response for 'FILE_NOT_FOUND'.");
        IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse =
                clientReceiver.waitForMessage(IdentifyPillarsForDeleteFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.FILE_NOT_FOUND_FAILURE);

        addStep("Validate the content of the cache", "Should not contain the checksum of the file");
        Assert.assertNull(cache.getChecksum(DEFAULT_FILE_ID));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarDeleteFileTestFailedNoSuchFileDuringOperation() {
        addDescription("Tests the DeleteFile functionality of the reference pillar for the scenario when the file " +
                "does not exist.");
        
        addStep("Send a deleteFileRequest for a none-existing file.",
                "A FILE_NOT_FOUND_FAILURE response should be return, and the original file should remain in the cache.");
        messageBus.sendMessage(msgFactory.createDeleteFileRequest(csData, csSpec, "NoneExistingFile"));

        DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
        Assert.assertNull(cache.getChecksum(DEFAULT_FILE_ID));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarDeleteFileTestFailedWrongChecksum() {
        addDescription("Tests the DeleteFile functionality of the reference pillar for scenario when a wrong "
                + "checksum is given as argument.");
        initializeCacheWithMD5ChecksummedFile();
        
        addStep("Create and send a DeleteFileRequest to the pillar contain a invalid checksum.",
                "A EXISTING_FILE_CHECKSUM_FAILURE response should be sent, an alarm should be generated. " +
                        "The checksum for the original file should remian in the cache.");
        ChecksumDataForFileTYPE invalidChecksumData = csData;
        invalidChecksumData.setChecksumValue(Base16Utils.encodeBase16("2234cccccccc4321"));
        messageBus.sendMessage(msgFactory.createDeleteFileRequest(invalidChecksumData, csSpec, DEFAULT_FILE_ID));
        DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(),
                ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE);
        Assert.assertNotNull(alarmReceiver.waitForMessage(AlarmMessage.class));
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarDeleteFileTestBadChecksumSpec() {
        addDescription("Test the handling of invalid checksumspecification.");
        initializeCacheWithMD5ChecksummedFile();

        addStep("Create and send a DeleteFileRequest to the pillar contain a invalid checksum specification type.",
                "A REQUEST_NOT_UNDERSTOOD_FAILURE response should be sent, an alarm should be generated. " +
                        "The checksum for the original file should remian in the cache.");

        ChecksumSpecTYPE badChecksumSpec = csSpec;
        csSpec.setOtherChecksumType("UNSUPPORTED ALGORITHM");
        messageBus.sendMessage(msgFactory.createDeleteFileRequest(csData, badChecksumSpec,DEFAULT_FILE_ID));
        DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.REQUEST_NOT_SUPPORTED);
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID), DEFAULT_MD5_CHECKSUM);
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarDeleteFileTestMissingChecksumArgument() {
        addDescription("Tests that a missing 'ChecksumOnExistingFile' will not delete the file.");

        context.getSettings().getRepositorySettings().getProtocolSettings().setRequireChecksumForDestructiveRequests(true);
        initializeCacheWithMD5ChecksummedFile();
        messageBus.sendMessage(msgFactory.createDeleteFileRequest(null, null, DEFAULT_FILE_ID));
        DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE);
        Assert.assertTrue(cache.hasFile(DEFAULT_FILE_ID));
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarDeleteFileTestAllowedMissingChecksum() {
        addDescription("Tests that a missing 'ChecksumOnExistingFile' will delete the file, when it has been allowed "
                + "to perform destructive operations in the settings.");
        context.getSettings().getRepositorySettings().getProtocolSettings().setRequireChecksumForDestructiveRequests(false);
        initializeCacheWithMD5ChecksummedFile();
        messageBus.sendMessage(msgFactory.createDeleteFileRequest(null, null, DEFAULT_FILE_ID));
        DeleteFileFinalResponse finalResponse = clientReceiver.waitForMessage(DeleteFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.OPERATION_COMPLETED);
        Assert.assertFalse(cache.hasFile(DEFAULT_FILE_ID));
    }
}
