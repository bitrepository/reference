/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integration;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.client.TestEventHandler;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.deletefile.DeleteFileClient;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.modify.replacefile.ReplaceFileClient;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.net.URL;
import java.util.Date;

/**
 * Test class for the reference pillar.
 *
 * ToDo needs a general refactoring to stabilize and split into focused tests. This should also be used as the
 * foundation for at general acceptance test of a running pillar. The refererence pilalr has it's own test.
 */
public class ReferencePillarIntegrationTest extends DefaultFixturePillarTest {
    @Override
    protected String getComponentID() {
        return "ReferencePillarUnderTest";
    }

    //ToDo needs a general refactoring to stabilize and split into focused tests.
    private static final String TEST_CLIENT_ID = "test-putClient";
//    @Test(groups = {"regressiontest"})
    @Test(groups = {"integrationtest"})
    public void testPillarVsClients() throws Exception {
        addDescription("Tests the put functionality of the reference pillar.");
        addStep("Set up constants and variables.", "Should not fail here!");
        //PillarComponentFactory.getINSTANCE().createReferencePillar(messageBus, settingsForCUT, "ReferencePillarUnderTest");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        String REPLACE_FILE_ADDRES = "http://sandkasse-01.kb.dk/dav/dia.jpg";
        Long FILE_SIZE = 27L;
        Long REPLACE_FILE_SIZE = 59898L;
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
//        String FILE_ID = DEFAULT_FILE_ID;
        String CHECKSUM_STRING = "940a51b250e7aa82d8e8ea31217ff267";
        byte[] CHECKSUM = Base16Utils.encodeBase16(CHECKSUM_STRING);
        String CHECKSUM_NEW_FILE_STRING = "f1d6f02be917ed6cdade56fa60653918";
        byte[] CHECKSUM_NEW_FILE = Base16Utils.encodeBase16(CHECKSUM_NEW_FILE_STRING);
        ChecksumSpecTYPE DEFAULT_CHECKSUM_TYPE = new ChecksumSpecTYPE();
        DEFAULT_CHECKSUM_TYPE.setChecksumSalt(null);
        DEFAULT_CHECKSUM_TYPE.setChecksumType(ChecksumType.MD5);
        
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        
        XMLFileSettingsLoader settingsLoader = new XMLFileSettingsLoader("settings/xml/bitrepository-devel");
        SettingsProvider provider = new SettingsProvider(settingsLoader, "TEST-putClient");
        Settings clientSettings = provider.getSettings();
        clientSettings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        clientSettings.getCollectionSettings().getClientSettings().getPillarIDs().add(settingsForCUT.getComponentID());
        
        addStep("Create a putclient and start a put operation.", 
                "This should be caught by the pillar.");
        PutFileClient putClient = ModifyComponentFactory.getInstance().retrievePutClient(clientSettings, securityManager, 
                TEST_CLIENT_ID);
        putClient.putFile(new URL(FILE_ADDRESS), FILE_ID, FILE_SIZE, 
                (ChecksumDataForFileTYPE) null, (ChecksumSpecTYPE) null, testEventHandler, "TEST-AUDIT-TRAIL");
        
        addStep("Validate the sequence of operations event for the putclient", "Shoud be in correct order.");
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
        
        addStep("Create a GetFileClient and start a get operation", 
                "This should be caught by the pillar");
        GetFileClient getClient = AccessComponentFactory.getInstance().createGetFileClient(clientSettings,
                securityManager, TEST_CLIENT_ID);
        getClient.getFileFromSpecificPillar(FILE_ID, null, new URL(FILE_ADDRESS), 
                settingsForCUT.getComponentID(), testEventHandler, null);
        
        addStep("Validate the sequence of operations event for the GetFileClient", 
                "Shoud be in correct order.");
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
        
        addStep("Create a GetChecksumsClient and start a get operation", 
                "This should be caught by the pillar");
        GetChecksumsClient getChecksums = AccessComponentFactory.getInstance().createGetChecksumsClient(clientSettings,
                securityManager, TEST_CLIENT_ID);
        FileIDs fileIDsForGetChecksums = new FileIDs();
        fileIDsForGetChecksums.setFileID(FILE_ID);
        
        URL csurl = new URL(FILE_ADDRESS + "-cs");
        
        getChecksums.getChecksums(clientSettings.getCollectionSettings().getClientSettings().getPillarIDs(),
                fileIDsForGetChecksums, DEFAULT_CHECKSUM_TYPE, csurl, testEventHandler, "AuditTrail: TESTING!!!");
        
        addStep("Validate the sequence of operation events for the getChecksumClient", 
                "Should be in correct order.");
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
        
        addStep("Create a GetFileIDsClient and start a get operation", 
                "This should be caught by the pillar");
        GetFileIDsClient getFileIDs = AccessComponentFactory.getInstance().createGetFileIDsClient(clientSettings, 
                securityManager, TEST_CLIENT_ID);
        FileIDs fileIdsForGetFileIDs = new FileIDs();
        fileIdsForGetFileIDs.setFileID(FILE_ID);
        
        URL fileIDsUrl = new URL(FILE_ADDRESS + "-id");
        
        getFileIDs.getFileIDs(clientSettings.getCollectionSettings().getClientSettings().getPillarIDs(),
                fileIdsForGetFileIDs, fileIDsUrl, testEventHandler);
        
        addStep("Validate the sequence of operation events for the getChecksumClient", 
                "Should be in correct order.");
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);

        addStep("Create a ReplaceFileClient at start a replace operation", 
                "This should be caught and handled by the pillar.");
        ReplaceFileClient replaceFile = ModifyComponentFactory.getInstance().retrieveReplaceFileClient(clientSettings, 
                securityManager, TEST_CLIENT_ID);

        ChecksumSpecTYPE checksumRequested = new ChecksumSpecTYPE();
        checksumRequested.setChecksumSalt(null);
        checksumRequested.setChecksumType(ChecksumType.SHA1);
        ChecksumDataForFileTYPE checksumDataOldFile = new ChecksumDataForFileTYPE();
        checksumDataOldFile.setChecksumSpec(DEFAULT_CHECKSUM_TYPE);
        checksumDataOldFile.setChecksumValue(CHECKSUM);
        checksumDataOldFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        
        ChecksumDataForFileTYPE checksumDataNewFile = new ChecksumDataForFileTYPE();
        checksumDataNewFile.setChecksumSpec(DEFAULT_CHECKSUM_TYPE);
        checksumDataNewFile.setChecksumValue(CHECKSUM_NEW_FILE);
        checksumDataNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        
        replaceFile.replaceFile(FILE_ID, settingsForCUT.getComponentID(),
                checksumDataOldFile, checksumRequested, new URL(REPLACE_FILE_ADDRES), REPLACE_FILE_SIZE, 
                checksumDataNewFile, checksumRequested, testEventHandler, "AuditTrail: TESTING!!!");
        
        addStep("Validate the sequence of operation events for the ReplaceFileClient", 
                "Should be in correct order.");
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
        
        addStep("Create a DeleteFileClient and start a delete operation", 
                "This should be caught by the pillar");
        DeleteFileClient deleteFile = ModifyComponentFactory.getInstance().retrieveDeleteFileClient(clientSettings,
                securityManager, TEST_CLIENT_ID);

        deleteFile.deleteFile(FILE_ID, settingsForCUT.getComponentID(),
                checksumDataNewFile, checksumRequested, testEventHandler, "AuditTrail: TESTING!!!");
        
        addStep("Validate the sequence of operation events for the DeleteFileClient", 
                "Should be in correct order.");
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFY_REQUEST_SENT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_IDENTIFIED);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.IDENTIFICATION_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.REQUEST_SENT);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.PROGRESS);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPONENT_COMPLETE);
        Assert.assertEquals(testEventHandler.waitForEvent().getEventType(), OperationEventType.COMPLETE);
    }

}
