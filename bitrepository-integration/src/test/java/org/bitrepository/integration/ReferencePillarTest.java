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

import java.net.URL;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.access.getfile.GetFileClient;
import org.bitrepository.access.getfileids.GetFileIDsClient;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.clienttest.TestEventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.modify.ModifyComponentFactory;
import org.bitrepository.modify.deletefile.DeleteFileClient;
import org.bitrepository.modify.putfile.PutFileClient;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.ReferencePillar;
import org.bitrepository.pillar.ReferencePillarComponentFactory;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Test class for the reference pillar.
 */
public class ReferencePillarTest extends DefaultFixturePillarTest {
    
    @Test(groups = {"regressiontest"})
    public void testPillarVsClients() throws Exception {
        addDescription("Tests the put functionality of the reference pillar.");
        addStep("Set up constants and variables.", "Should not fail here!");
        settings.getReferenceSettings().getClientSettings().setReceiverDestination("TEST-pillar-destination-jolf");
        ReferencePillar pillar = ReferencePillarComponentFactory.getInstance().getPillar(settings);
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        Long FILE_SIZE = 27L;
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String CHECKSUM = "940a51b250e7aa82d8e8ea31217ff267";
        ChecksumSpecTYPE DEFAULT_CHECKSUM_TYPE = new ChecksumSpecTYPE();
        DEFAULT_CHECKSUM_TYPE.setChecksumSalt(null);
        DEFAULT_CHECKSUM_TYPE.setChecksumType("MD5");

        
        TestEventHandler testEventHandler = new TestEventHandler(testEventManager);
        
        XMLFileSettingsLoader settingsLoader = new XMLFileSettingsLoader("settings/xml");
        SettingsProvider provider = new SettingsProvider(settingsLoader);
        Settings clientSettings = provider.getSettings("bitrepository-devel");
        clientSettings.getCollectionSettings().setProtocolSettings(settings.getCollectionSettings().getProtocolSettings());
        clientSettings.getCollectionSettings().setClientSettings(settings.getCollectionSettings().getClientSettings());
        clientSettings.getReferenceSettings().getClientSettings().setReceiverDestination("TEST-client-destination");
        clientSettings.getCollectionSettings().getClientSettings().getPillarIDs().clear();
        clientSettings.getCollectionSettings().getClientSettings().getPillarIDs().add(settings.getReferenceSettings().getPillarSettings().getPillarID());
        
        addStep("Create a putclient and start a put operation.", 
                "This should be caught by the pillar.");
        PutFileClient putClient = ModifyComponentFactory.getInstance().retrievePutClient(clientSettings);
        putClient.putFileWithId(new URL(FILE_ADDRESS), FILE_ID, FILE_SIZE, testEventHandler);
        
        addStep("Validate the sequence of operations event for the putclient", "Shoud be in correct order.");
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IdentifyPillarsRequestSent);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarComplete);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
        
        addStep("Create a GetFileClient and start a get operation", 
                "This should be caught by the pillar");
        GetFileClient getClient = AccessComponentFactory.getInstance().createGetFileClient(clientSettings);
        getClient.getFileFromSpecificPillar(FILE_ID, new URL(FILE_ADDRESS), settings.getReferenceSettings().getPillarSettings().getPillarID(), testEventHandler);
        
        addStep("Validate the sequence of operations event for the GetFileClient", 
                "Shoud be in correct order.");
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IdentifyPillarsRequestSent);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
        
        addStep("Create a GetChecksumsClient and start a get operation", 
                "This should be caught by the pillar");
        GetChecksumsClient getChecksums = AccessComponentFactory.getInstance().createGetChecksumsClient(clientSettings);
        FileIDs fileIDsForGetChecksums = new FileIDs();
        fileIDsForGetChecksums.setFileID(FILE_ID);
        
        URL csurl = new URL(FILE_ADDRESS + "-cs");
        
        getChecksums.getChecksums(clientSettings.getCollectionSettings().getClientSettings().getPillarIDs(), 
                fileIDsForGetChecksums, DEFAULT_CHECKSUM_TYPE, csurl, testEventHandler, "AuditTrail: TESTING!!!");
        
        addStep("Validate the sequence of operation events for the getChecksumClient", 
                "Should be in correct order.");
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IdentifyPillarsRequestSent);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarComplete);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
        
        addStep("Create a GetFileIDsClient and start a get operation", 
                "This should be caught by the pillar");
        GetFileIDsClient getFileIDs = AccessComponentFactory.getInstance().createGetFileIDsClient(clientSettings);
        FileIDs fileIdsForGetFileIDs = new FileIDs();
        fileIdsForGetFileIDs.setFileID(FILE_ID);
        
        URL fileIDsUrl = new URL(FILE_ADDRESS + "-id");
        
        getFileIDs.getFileIDs(clientSettings.getCollectionSettings().getClientSettings().getPillarIDs(), 
                fileIdsForGetFileIDs, fileIDsUrl, testEventHandler, "AuditTrail: TESTING!!!");
        
        addStep("Validate the sequence of operation events for the getChecksumClient", 
                "Should be in correct order.");
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IdentifyPillarsRequestSent);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarComplete);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);

        addStep("Create a DeleteFileClient and start a delete operation", 
                "This should be caught by the pillar");
        DeleteFileClient deleteFile = ModifyComponentFactory.getInstance().retrieveDeleteFileClient(clientSettings);

        ChecksumSpecTYPE checksumRequested = new ChecksumSpecTYPE();
        checksumRequested.setChecksumSalt(null);
        checksumRequested.setChecksumType("SHA-1");
        deleteFile.deleteFile(FILE_ID, settings.getReferenceSettings().getPillarSettings().getPillarID(), 
                CHECKSUM, DEFAULT_CHECKSUM_TYPE, checksumRequested, testEventHandler, "AuditTrail: TESTING!!!");
        
        addStep("Validate the sequence of operation events for the DeleteFileClient", 
                "Should be in correct order.");
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.IdentifyPillarsRequestSent);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarIdentified);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarSelected);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.RequestSent);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Progress);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.PillarComplete);
        Assert.assertEquals(testEventHandler.waitForEvent().getType(), OperationEventType.Complete);
    }
}
